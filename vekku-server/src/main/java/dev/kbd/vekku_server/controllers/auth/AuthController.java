package dev.kbd.vekku_server.controllers.auth;

import dev.kbd.vekku_server.controllers.auth.models.LoginRequest;
import dev.kbd.vekku_server.controllers.auth.models.LoginResponse;
import dev.kbd.vekku_server.controllers.auth.models.SignupRequest;
import dev.kbd.vekku_server.controllers.auth.models.UserInfo;
import dev.kbd.vekku_server.controllers.auth.models.VerifyOtpRequest;
import dev.kbd.vekku_server.controllers.mapper.AuthMapper;
import dev.kbd.vekku_server.infrastructure.ratelimiter.RateLimit;
import dev.kbd.vekku_server.services.auth.AuthService;
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
    private final AuthMapper authMapper;

    @PostMapping("/signup")
    @RateLimit(limit = 5, duration = 60)
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        authService.signup(authMapper.toSignupParam(request));
        return ResponseEntity.ok("OTP sent to email");
    }

    @PostMapping("/verify")
    @RateLimit(limit = 10, duration = 60)
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(authMapper.toVerifyOtpParam(request));
        return ResponseEntity.ok("User verified and registered successfully");
    }

    @PostMapping("/login")
    @RateLimit(limit = 5, duration = 60)
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(authMapper.toLoginParam(request)));
    }

    @GetMapping("/me")
    @RateLimit(limit = 5, duration = 60)
    public ResponseEntity<UserInfo> getCurrentUser(
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(new UserInfo(
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name")));
    }
}
