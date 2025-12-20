package dev.kbd.vekku_server.services.auth;

import dev.kbd.vekku_server.controllers.auth.models.LoginResponse;
import dev.kbd.vekku_server.services.auth.model.LoginParam;
import dev.kbd.vekku_server.services.auth.model.SignupParam;
import dev.kbd.vekku_server.services.auth.model.VerifyOtpParam;

public interface AuthService {
    void signup(SignupParam param);

    void verifyOtp(VerifyOtpParam param);

    LoginResponse login(LoginParam param);
}
