package dev.kbd.vekku_server.orchestrators;

import dev.kbd.vekku_server.services.auth.AuthService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthOrchestrator {

    final AuthService authService;

    final Map<String, PendingUser> tempUsers = new HashMap<>();

    public String startSignUp(
        String email,
        String password,
        String firstName,
        String lastName
    ) {
        log.info("Starting sign up process for user with email: {}", email);

        log.debug("Generating OTP");
        String otp = generateOTP();
        log.debug("OTP generated: {}", otp);

        log.debug("Generating Token..");
        String token = UUID.randomUUID().toString();
        log.debug("Generated token : {}...", token.substring(0, 5));

        log.debug("Mapping token {} to email {}, OTP : {}", token, email, otp);
        tempUsers.put(
            token,
            new PendingUser(email, password, firstName, lastName, otp)
        );

        log.debug(
            "Mapping OTP: {} signIn data {} {} {}",
            otp,
            email,
            firstName,
            lastName
        );
        tempUsers.put(
            otp,
            new PendingUser(email, password, firstName, lastName, otp)
        );
        return token;
    }

    public void verifyOtp(String token, String otp) {
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

        log.debug("Valid OTP {} for token: {}", otp, token);
        log.debug("Creating user...");
        authService.createUser(
            pendingUser.email(),
            pendingUser.password(),
            pendingUser.firstName(),
            pendingUser.lastName()
        );
        log.debug("User created");
    }

    private String generateOTP() {
        // Generate a random OTP
        return String.valueOf((int) (Math.random() * 1000000));
    }
}

record PendingUser(
    String email,
    String password,
    String firstName,
    String lastName,
    String otp
) {}
