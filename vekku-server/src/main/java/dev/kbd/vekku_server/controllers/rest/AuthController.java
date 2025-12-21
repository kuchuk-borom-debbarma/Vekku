package dev.kbd.vekku_server.controllers.rest;

import dev.kbd.vekku_server.dtos.UserInfo;
import dev.kbd.vekku_server.infrastructure.ratelimiter.RateLimit;
import dev.kbd.vekku_server.orchestrators.AuthOrchestrator;
import dev.kbd.vekku_server.services.auth.AuthService;
import dev.kbd.vekku_server.services.auth.AuthService.LoginData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    final AuthService authService;
    final AuthOrchestrator authOrchestrator;

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequest request) {
        log.info("Signing up user with email: {}", request.email());
        String token = authOrchestrator.startSignUp(
            request.email(),
            request.password(),
            request.firstName(),
            request.lastName()
        );
        log.debug(
            "User signed up successfully. Token: ...{}...",
            token.substring(5, 10)
        );
        return ResponseEntity.ok(token);
    }

    @PostMapping("/verify")
    public void verify(
        @Header("otp") String otp,
        @Header("token") String token
    ) {
        log.info("Verifying user with token: {}", token);
        authOrchestrator.verifyOtp(token, otp);
    }

    @RateLimit
    @PostMapping("/signin")
    public LoginData signIn(@RequestBody SignInRequest request) {
        log.info("Signing in user with email: {}", request.email());
        LoginData loginData = authService.login(
            request.email(),
            request.password()
        );
        if (loginData == null) {
            log.warn("Failed to sign in user with email: {}", request.email());
        }
        return loginData;
    }

    @RateLimit
    @GetMapping("/")
    public ResponseEntity<UserInfo> getUserInfo(
        @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(
            new UserInfo(
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name")
            )
        );
    }
}

record SignInRequest(String email, String password) {}

record SignUpRequest(
    String email,
    String password,
    String firstName,
    String lastName
) {}
