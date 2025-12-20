package dev.kbd.vekku_server.controllers.auth.models;

public record LoginResponse(String accessToken, String refreshToken, long expiresIn) {
}
