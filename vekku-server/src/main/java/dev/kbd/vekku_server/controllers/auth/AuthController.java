package dev.kbd.vekku_server.controllers.auth;

import dev.kbd.vekku_server.infrastructure.ratelimiter.RateLimit;
import dev.kbd.vekku_server.services.auth.AuthService;
import dev.kbd.vekku_server.services.auth.dto.LoginData;
import dev.kbd.vekku_server.services.auth.dto.LoginParam;
import dev.kbd.vekku_server.services.auth.dto.SignupParam;
import dev.kbd.vekku_server.services.auth.dto.User;
import dev.kbd.vekku_server.services.auth.dto.VerifyOtpParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @RateLimit(limit = 5, duration = 60)
    public ResponseEntity<String> signup(@RequestBody SignupParam request) {
        authService.signup(request);
        return ResponseEntity.ok("OTP sent to email");
    }

    @PostMapping("/verify")
    @RateLimit(limit = 10, duration = 60)
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpParam request) {
        authService.verifyOtp(request);
        return ResponseEntity.ok("User verified and registered successfully");
    }

    @PostMapping("/login")
    @RateLimit(limit = 5, duration = 60)
    public ResponseEntity<LoginData> login(
            @RequestBody LoginParam request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @RateLimit(limit = 5, duration = 60)
    public ResponseEntity<User> getCurrentUser(
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(new User(
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name")));
    }
}
