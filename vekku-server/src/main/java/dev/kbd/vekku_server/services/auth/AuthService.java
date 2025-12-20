package dev.kbd.vekku_server.services.auth;

import dev.kbd.vekku_server.services.auth.dto.LoginData;
import dev.kbd.vekku_server.services.auth.dto.LoginParam;
import dev.kbd.vekku_server.services.auth.dto.SignupParam;
import dev.kbd.vekku_server.services.auth.dto.VerifyOtpParam;

public interface AuthService {
    void signup(SignupParam param);

    void verifyOtp(VerifyOtpParam param);

    LoginData login(LoginParam param);
}
