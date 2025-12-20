package dev.kbd.vekku_server.services.auth.dto;

public record SignupParam(String email, String password, String firstName, String lastName) {
}
