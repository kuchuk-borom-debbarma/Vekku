package dev.kbd.vekku_server.auth.api;

public class AuthDTOs {

    private AuthDTOs() {}

    public record UserInfoDTO(
        String id,
        String email,
        String firstName,
        String lastName
    ) {}
}
