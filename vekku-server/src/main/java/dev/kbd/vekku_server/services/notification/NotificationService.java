package dev.kbd.vekku_server.services.notification;

public interface NotificationService {
    void sendOtp(String email, String otp);
}
