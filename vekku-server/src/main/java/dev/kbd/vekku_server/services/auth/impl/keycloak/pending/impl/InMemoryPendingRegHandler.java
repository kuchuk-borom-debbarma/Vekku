package dev.kbd.vekku_server.services.auth.impl.keycloak.pending.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import dev.kbd.vekku_server.services.auth.impl.keycloak.pending.PendingRegistrationHandler;
import dev.kbd.vekku_server.services.auth.impl.keycloak.pending.model.PendingRegistration;

@Service
public class InMemoryPendingRegHandler implements PendingRegistrationHandler {
    private final Map<String, PendingRegistration> pendingRegistrations = new ConcurrentHashMap<>();

    @Override
    public void addPendingRegistration(PendingRegistration registration) {
        pendingRegistrations.put(registration.param().email(), registration);
        ;
    }

    @Override
    public PendingRegistration getPendingRegistration(String email) {
        return pendingRegistrations.get(email);
    }

    @Override
    public void removePendingRegistration(String email) {
        pendingRegistrations.remove(email);
    }

}
