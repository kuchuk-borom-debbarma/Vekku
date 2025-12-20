package dev.kbd.vekku_server.controllers.auth.models;

public record VerifyOtpRequest(String email, String otp) {
}
