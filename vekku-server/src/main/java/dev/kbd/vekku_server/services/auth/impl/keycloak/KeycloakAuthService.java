package dev.kbd.vekku_server.services.auth.impl.keycloak;

import dev.kbd.vekku_server.controllers.auth.models.LoginResponse;
import dev.kbd.vekku_server.services.auth.AuthService;
import dev.kbd.vekku_server.services.auth.impl.keycloak.pending.PendingRegistrationHandler;
import dev.kbd.vekku_server.services.auth.impl.keycloak.pending.model.PendingRegistration;
import dev.kbd.vekku_server.services.auth.model.LoginParam;
import dev.kbd.vekku_server.services.auth.model.SignupParam;
import dev.kbd.vekku_server.services.auth.model.VerifyOtpParam;
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
import java.util.Random;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAuthService implements AuthService {

    private final NotificationService notificationService;
    private final PendingRegistrationHandler pendingRegistrationHandler;

    @Value("${keycloak.auth-server-url:http://localhost:8180}")
    private String serverUrl;

    @Value("${keycloak.realm:vekku}")
    private String realm;

    @Value("${keycloak.admin-username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin-password:admin}")
    private String adminPassword;

    private Keycloak keycloak;

    // Build admin client
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
    public void signup(SignupParam param) {
        log.info("Processing signup for email: {}", param.email());

        // Check if user exists
        List<UserRepresentation> existing = keycloak.realm(realm).users().searchByEmail(param.email(), true);
        if (!existing.isEmpty()) {
            throw new RuntimeException("User already exists");
        }

        // Generate OTP
        String otp = generateOtp();
        pendingRegistrationHandler.addPendingRegistration(new PendingRegistration(otp, param));

        // Send OTP
        notificationService.sendOtp(param.email(), otp);
    }

    @Override
    public void verifyOtp(VerifyOtpParam param) {
        log.info("Verifying OTP for email: {}", param.email());
        PendingRegistration pending = pendingRegistrationHandler.getPendingRegistration(param.email());

        if (pending == null || !pending.otp().equals(param.otp())) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        // OTP Valid. Create User in Keycloak.
        createUserInKeycloak(pending.param());

        // Cleanup
        pendingRegistrationHandler.removePendingRegistration(param.email());
    }

    @Override
    public LoginResponse login(LoginParam param) {
        log.info("Attempting login for user: {}", param.email());

        // Use KeycloakBuilder to act as the user and get a token
        try (Keycloak userKeycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId("vekku-client")
                // .clientSecret("") // Public client
                .username(param.email())
                .password(param.password())
                .scope("openid profile email") // REQUIRED for UserInfo endpoint
                .build()) {

            AccessTokenResponse tokenResponse = userKeycloak.tokenManager().getAccessToken();

            return new LoginResponse(
                    tokenResponse.getToken(),
                    tokenResponse.getRefreshToken(),
                    tokenResponse.getExpiresIn());
        } catch (Exception e) {
            log.error("Login failed for user {}", param.email(), e);
            throw new RuntimeException("Invalid credentials");
        }
    }

    private void createUserInKeycloak(SignupParam request) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.email());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(true);
        user.setEmailVerified(true);

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
            var roleRep = keycloak.realm(realm).roles().get("USER").toRepresentation();
            keycloak.realm(realm).users().get(userId).roles().realmLevel().add(List.of(roleRep));

        } catch (Exception e) {
            log.error("Failed to assign USER role to user {}", userId, e);
        }
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
