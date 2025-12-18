package dev.kbd.vekku_server.services.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoggedNotificationService implements NotificationService {

    @Override
    public void sendOtp(String email, String otp) {
        log.info("[OTP-LOG] Sending OTP {} to email {}", otp, email);
    }
}
