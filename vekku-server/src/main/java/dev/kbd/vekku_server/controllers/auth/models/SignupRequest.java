package dev.kbd.vekku_server.controllers.auth.models;

public record SignupRequest(String email, String password, String firstName, String lastName) {
}
