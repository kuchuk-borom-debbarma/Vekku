package dev.kbd.vekku_server.services.auth;

import dev.kbd.vekku_server.dto.auth.SignupRequest;
import dev.kbd.vekku_server.dto.auth.VerifyOtpRequest;
import dev.kbd.vekku_server.services.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
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

    private void createUserInKeycloak(SignupRequest request) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.email());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(true);
        user.setEmailVerified(true);
        // user.setRealmRoles(List.of("USER")); // Can be set if role exists

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
        log.info("User created successfully in Keycloak");
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
