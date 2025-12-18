package dev.kbd.vekku_server.dto.auth;

public record SignupRequest(String email, String password, String firstName, String lastName) {
}
