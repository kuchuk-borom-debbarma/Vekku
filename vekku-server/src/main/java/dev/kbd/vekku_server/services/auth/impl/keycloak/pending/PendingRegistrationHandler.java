package dev.kbd.vekku_server.services.auth.impl.keycloak.pending;

import dev.kbd.vekku_server.services.auth.impl.keycloak.pending.model.PendingRegistration;

public interface PendingRegistrationHandler {
    void addPendingRegistration(PendingRegistration registration);

    PendingRegistration getPendingRegistration(String email);

    void removePendingRegistration(String email);
}
