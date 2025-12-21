package dev.kbd.vekku_server.auth;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
class KeycloakAuthService implements IAuthService {

    @Value("${keycloak.auth-server-url:http://localhost:8180}")
    private String serverUrl;

    @Value("${keycloak.realm:vekku}")
    private String realm;

    @Value("${keycloak.admin-username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin-password:admin}")
    private String adminPassword;

    private Keycloak keycloak;

    private final Map<String, PendingUser> tempUsers =
        new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        keycloak = KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .realm("master")
            .clientId("admin-cli")
            .username(adminUsername)
            .password(adminPassword)
            .build();
    }

    @Override
    public String startSignUp(
        String email,
        String password,
        String firstName,
        String lastName
    ) {
        log.info("Starting sign up process for user with email: {}", email);

        log.debug("Generating OTP");
        String otp = generateOTP();

        log.debug("Generating Token..");
        String token = UUID.randomUUID().toString();

        // Store the pending user data mapped by Token
        tempUsers.put(
            token,
            new PendingUser(email, password, firstName, lastName, otp)
        );

        tempUsers.put(
            token,
            new PendingUser(email, password, firstName, lastName, otp)
        );

        log.debug(
            "Mapped token {} to email {}, OTP : {}",
            token.substring(0, 5),
            email,
            otp
        );

        return token;
    }

    @Override
    public void verifySignUp(String otp, String token) {
        log.info("Verifying OTP for token: {}", token);

        PendingUser pendingUser = tempUsers.get(token);

        if (pendingUser == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Token not found"
            );
        }

        if (!pendingUser.otp().equals(otp)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid OTP"
            );
        }

        log.debug("Valid OTP for token. Creating user...");

        // Call internal create method
        createUserInKeycloak(
            pendingUser.email(),
            pendingUser.password(),
            pendingUser.firstName(),
            pendingUser.lastName()
        );

        // Cleanup
        tempUsers.remove(token);

        log.debug("User created and temporary data cleaned up.");
    }

    private String generateOTP() {
        return String.valueOf((int) (Math.random() * 1000000));
    }

    @Override
    public LoginData login(String email, String password) {
        log.info("Attempting login for user: {}", email);
        try (
            Keycloak userKeycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId("vekku-client")
                .username(email)
                .password(password)
                .scope("openid profile email")
                .build()
        ) {
            AccessTokenResponse tokenResponse = userKeycloak
                .tokenManager()
                .getAccessToken();
            return new LoginData(
                tokenResponse.getToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getExpiresIn()
            );
        } catch (Exception e) {
            log.error("Login failed for user {}", email, e);
            throw new RuntimeException("Invalid credentials");
        }
    }

    @Override
    public void createUser(
        String email,
        String password,
        String firstName,
        String lastName
    ) {
        // Direct creation (bypassing OTP)
        createUserInKeycloak(email, password, firstName, lastName);
    }

    private void createUserInKeycloak(
        String email,
        String password,
        String firstName,
        String lastName
    ) {
        log.info("Creating user in Keycloak: {}", email);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));

        Response response = keycloak.realm(realm).users().create(user);

        if (response.getStatus() != 201) {
            log.error(
                "Failed to create user in Keycloak. Status: {}",
                response.getStatus()
            );
            throw new RuntimeException("Failed to register user");
        }

        String userId = CreatedResponseUtil.getCreatedId(response);
        assignUserRole(userId);
        log.info("User created successfully in Keycloak with ID: {}", userId);
    }

    private void assignUserRole(String userId) {
        try {
            var roleRep = keycloak
                .realm(realm)
                .roles()
                .get("USER")
                .toRepresentation();
            keycloak
                .realm(realm)
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .add(List.of(roleRep));
        } catch (Exception e) {
            log.error("Failed to assign USER role to user {}", userId, e);
        }
    }
}

// You can keep this in the same file or move to its own file
record PendingUser(
    String email,
    String password,
    String firstName,
    String lastName,
    String otp
) {}
