package dev.kbd.vekku_server.dto.auth;

public record VerifyOtpRequest(String email, String otp) {
}
