package dev.kbd.vekku_server.controllers.mapper;

import org.mapstruct.Mapper;

import dev.kbd.vekku_server.controllers.auth.models.LoginRequest;
import dev.kbd.vekku_server.controllers.auth.models.SignupRequest;
import dev.kbd.vekku_server.controllers.auth.models.VerifyOtpRequest;
import dev.kbd.vekku_server.services.auth.model.LoginParam;
import dev.kbd.vekku_server.services.auth.model.SignupParam;
import dev.kbd.vekku_server.services.auth.model.VerifyOtpParam;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    // MapStruct detects fields with the same name automatically.
    // If field names differ, use @Mapping(source="...", target="...")
    SignupParam toSignupParam(SignupRequest request);

    VerifyOtpParam toVerifyOtpParam(VerifyOtpRequest request);

    LoginParam toLoginParam(LoginRequest request);
}
