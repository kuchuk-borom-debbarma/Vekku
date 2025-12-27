package dev.kbd.vekku_server.auth.api;

public class AuthDTOs {

    private AuthDTOs() {}

    public record UserInfoDTO(
        String id,
        String email,
        String firstName,
        String lastName
    ) {}

    public record SignInRequest(String email, String password) {}

    public record SignUpRequest(
        String email,
        String password,
        String firstName,
        String lastName
    ) {}
}
