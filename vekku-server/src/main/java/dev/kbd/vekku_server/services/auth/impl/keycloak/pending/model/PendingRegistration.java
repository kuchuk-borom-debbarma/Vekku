package dev.kbd.vekku_server.services.auth.impl.keycloak.pending.model;

import dev.kbd.vekku_server.services.auth.model.SignupParam;

public record PendingRegistration(String otp, SignupParam param) {
}