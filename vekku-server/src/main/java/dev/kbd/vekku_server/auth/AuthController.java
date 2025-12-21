package dev.kbd.vekku_server.auth;

import dev.kbd.vekku_server.auth.AuthDTOs.UserInfoDTO;
import dev.kbd.vekku_server.auth.IAuthService.LoginData;
import dev.kbd.vekku_server.infrastructure.ratelimiter.RateLimit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@RequiredArgsConstructor
class AuthController {

    final IAuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequest request) {
        log.info("Received signup request for email: {}", request.email());

        String token = authService.startSignUp(
            request.email(),
            request.password(),
            request.firstName(),
            request.lastName()
        );

        log.info(
            "Signup initiated successfully for email: {}. Verification token generated.",
            request.email()
        );
        return ResponseEntity.ok(token);
    }

    @PostMapping("/verify")
    public void verify(
        @RequestHeader("otp") String otp,
        @RequestHeader("token") String token
    ) {
        log.info("Received verification request for token: {}", token);

        authService.verifySignUp(otp, token);

        log.info("Verification successful for token: {}. User created.", token);
    }

    @RateLimit
    @PostMapping("/signin")
    public LoginData signIn(@RequestBody SignInRequest request) {
        log.info("Received signin request for email: {}", request.email());

        try {
            LoginData loginData = authService.login(
                request.email(),
                request.password()
            );
            log.info("User {} signed in successfully.", request.email());
            return loginData;
        } catch (Exception e) {
            log.warn(
                "Sign in failed for user: {}. Reason: {}",
                request.email(),
                e.getMessage()
            );
            throw e; // Re-throw to let the global exception handler or framework handle the 401/403
        }
    }

    @RateLimit
    @GetMapping("/")
    public ResponseEntity<UserInfoDTO> getUserInfo(
        @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt == null) {
            log.warn("UserInfo request rejected: No JWT principal found.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = jwt.getClaimAsString("email");
        log.debug("Fetching user info for authenticated user: {}", email);

        return ResponseEntity.ok(
            new UserInfoDTO(
                jwt.getSubject(),
                email,
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name")
            )
        );
    }
}

// DTOs
record SignInRequest(String email, String password) {}

record SignUpRequest(
    String email,
    String password,
    String firstName,
    String lastName
) {}
