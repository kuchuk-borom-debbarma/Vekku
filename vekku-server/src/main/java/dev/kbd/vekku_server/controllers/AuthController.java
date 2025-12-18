package dev.kbd.vekku_server.controllers;

import dev.kbd.vekku_server.dto.auth.SignupRequest;
import dev.kbd.vekku_server.dto.auth.VerifyOtpRequest;
import dev.kbd.vekku_server.services.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok("OTP sent to email");
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request);
        return ResponseEntity.ok("User verified and registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<dev.kbd.vekku_server.dto.auth.LoginResponse> login(
            @RequestBody dev.kbd.vekku_server.dto.auth.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
