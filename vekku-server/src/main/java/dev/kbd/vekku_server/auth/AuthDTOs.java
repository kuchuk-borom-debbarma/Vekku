package dev.kbd.vekku_server.auth;

class AuthDTOs {

    private AuthDTOs() {}

    public record UserInfoDTO(
        String id,
        String email,
        String firstName,
        String lastName
    ) {}
}
