package dev.kbd.vekku_server.services.auth;

import dev.kbd.vekku_server.dto.auth.LoginRequest;
import dev.kbd.vekku_server.dto.auth.LoginResponse;
import dev.kbd.vekku_server.dto.auth.SignupRequest;
import dev.kbd.vekku_server.dto.auth.VerifyOtpRequest;
import dev.kbd.vekku_server.services.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAuthService implements AuthService {

    private final NotificationService notificationService;

    // Store Pending Signup Request (Email -> Request)
    // We also store the generated OTP within a wrapper or side-by-side.
    // For simplicity, let's use a wrapper class or just a map of Email ->
    // PendingRegistration
    private final Map<String, PendingRegistration> pendingRegistrations = new ConcurrentHashMap<>();

    private record PendingRegistration(String otp, SignupRequest request) {
    }

    @Value("${keycloak.auth-server-url:http://localhost:8180}")
    private String serverUrl;

    @Value("${keycloak.realm:vekku}")
    private String realm;

    @Value("${keycloak.admin-username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin-password:admin}")
    private String adminPassword;

    // We are using 'admin-cli' client which is available by default in 'master'
    // realm
    // to manage other realms. Alternatively, we can use a service account in
    // 'vekku' realm.
    private Keycloak keycloak;

    @PostConstruct
    public void init() {
        keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master") // Admin operations usually done via master realm
                .clientId("admin-cli")
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }

    @Override
    public void signup(SignupRequest request) {
        log.info("Processing signup for email: {}", request.email());

        // Check if user already exists in Keycloak?
        // For simplicity/performance we might skip this or do a quick search.
        // Doing a search to be safe.
        List<UserRepresentation> existing = keycloak.realm(realm).users().searchByEmail(request.email(), true);
        if (!existing.isEmpty()) {
            throw new RuntimeException("User already exists");
        }

        // Generate OTP
        String otp = generateOtp();
        pendingRegistrations.put(request.email(), new PendingRegistration(otp, request));

        // Send OTP
        notificationService.sendOtp(request.email(), otp);
    }

    @Override
    public void verifyOtp(VerifyOtpRequest request) {
        log.info("Verifying OTP for email: {}", request.email());
        PendingRegistration pending = pendingRegistrations.get(request.email());

        if (pending == null || !pending.otp().equals(request.otp())) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        // OTP Valid. Create User in Keycloak.
        createUserInKeycloak(pending.request());

        // Cleanup
        pendingRegistrations.remove(request.email());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.email());

        // Use KeycloakBuilder to act as the user and get a token
        // usage of KeycloakBuilder.builder()...grantType(PASSWORD) acts as a client to
        // get user tokens
        // logic:
        try (Keycloak userKeycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId("vekku-client")
                // .clientSecret("") // Public client
                .username(request.email())
                .password(request.password())
                .scope("openid profile email") // REQUIRED for UserInfo endpoint
                .build()) {

            AccessTokenResponse tokenResponse = userKeycloak.tokenManager().getAccessToken();

            return new LoginResponse(
                    tokenResponse.getToken(),
                    tokenResponse.getRefreshToken(),
                    tokenResponse.getExpiresIn());
        } catch (Exception e) {
            log.error("Login failed for user {}", request.email(), e);
            throw new RuntimeException("Invalid credentials");
        }
    }

    @Override
    public dev.kbd.vekku_server.dto.auth.UserInfo getUserInfo(String token) {
        // We need to call the OIDC UserInfo endpoint.
        // We can use KeycloakBuilder (acting as client) or Resteasy directly. By
        // default java admin client
        // doesn't have a direct "validate token" or "user info" method easily
        // accessible without full configuration.
        // A simple way is to use the Admin Client's Realm resource if we had the ID,
        // but we only have the token.
        // So we will use the OpenID Connect UserInfo endpoint.

        // URL: http://localhost:8180/realms/vekku/protocol/openid-connect/userinfo
        // Header: Authorization: Bearer <token>

        String userInfoUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/userinfo";

        try (var client = jakarta.ws.rs.client.ClientBuilder.newClient()) {
            var response = client.target(userInfoUrl)
                    .request(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .get();

            if (response.getStatus() != 200) {
                throw new RuntimeException("Invalid token");
            }

            Map<String, Object> userInfo = response.readEntity(Map.class);

            return new dev.kbd.vekku_server.dto.auth.UserInfo(
                    (String) userInfo.get("sub"),
                    (String) userInfo.get("email"),
                    (String) userInfo.get("given_name"),
                    (String) userInfo.get("family_name"));
        } catch (Exception e) {
            log.error("Failed to fetch user info", e);
            throw new RuntimeException("Failed to fetch user info");
        }
    }

    private void createUserInKeycloak(SignupRequest request) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.email());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(true);
        user.setEmailVerified(true);
        // Note: setting realmRoles in UserRepresentation during create often doesn't
        // work
        // with some Keycloak versions/admin clients. Safer to add role mappings after
        // creation.
        // But let's try direct setting if the DTO supports it, otherwise explicit
        // mapping.

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.password());
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));

        Response response = keycloak.realm(realm).users().create(user);

        if (response.getStatus() != 201) {
            log.error("Failed to create user in Keycloak. Status: {}", response.getStatus());
            throw new RuntimeException("Failed to register user");
        }

        // Fetch created user to get ID
        String userId = CreatedResponseUtil.getCreatedId(response);

        // Assign USER role
        assignUserRole(userId);

        log.info("User created successfully in Keycloak with ID: {}", userId);
    }

    private void assignUserRole(String userId) {
        try {
            // Get role representation
            // logic: realm().roles().get("USER").toRepresentation()
            // then realm().users().get(userId).roles().realmLevel().add(list)

            var roleRep = keycloak.realm(realm).roles().get("USER").toRepresentation();
            keycloak.realm(realm).users().get(userId).roles().realmLevel().add(List.of(roleRep));

        } catch (Exception e) {
            log.error("Failed to assign USER role to user {}", userId, e);
            // Non-blocking failure? OR blocking?
            // In a real app we might want to retry or transactionalize this.
            // For now, log error.
        }
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
