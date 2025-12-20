package dev.kbd.vekku_server.services.auth.dto;

public record LoginData(String accessToken, String refreshToken, long expiresIn) {

}
