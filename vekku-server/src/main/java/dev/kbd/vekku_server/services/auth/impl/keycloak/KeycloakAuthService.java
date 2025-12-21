package dev.kbd.vekku_server.services.auth.impl.keycloak;

import dev.kbd.vekku_server.services.auth.AuthService;
import dev.kbd.vekku_server.services.notification.NotificationService;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Random;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAuthService implements AuthService {

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
    public LoginData login(String email, String password) {
        log.info("Attempting login for user: {}", email);

        // Use KeycloakBuilder to act as the user and get a token
        try (
            Keycloak userKeycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId("vekku-client")
                // .clientSecret("") // Public client
                .username(email)
                .password(password)
                .scope("openid profile email") // REQUIRED for UserInfo endpoint
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
        log.info("Creating user with email: {} in keyclock", email);
        createUserInKeycloak(email, password, firstName, lastName);
    }

    private void createUserInKeycloak(
        String email,
        String password,
        String firstName,
        String lastName
    ) {
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

        // Fetch created user to get ID
        String userId = CreatedResponseUtil.getCreatedId(response);

        // Assign USER role
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
