package dev.kbd.vekku_server.services.auth;

import dev.kbd.vekku_server.dto.auth.SignupRequest;
import dev.kbd.vekku_server.dto.auth.VerifyOtpRequest;

public interface AuthService {
    void signup(SignupRequest request);

    void verifyOtp(VerifyOtpRequest request);
}
