package com.paisa.backend.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OtpSmsService {

    @Value("${app.twilio.account-sid}")
    private String accountSid;

    @Value("${app.twilio.auth-token}")
    private String authToken;

    @Value("${app.twilio.from-number}")
    private String fromNumber;

    @Value("${app.twilio.enabled}")
    private boolean enabled;

    @PostConstruct
    public void init() {
        if (enabled) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio SMS service initialized");
        } else {
            log.warn("SMS sending DISABLED — OTPs will log to console only");
        }
    }

    public void sendOtp(String toPhone, String otp) {
        String messageBody = String.format(
                "Your ExpenseTracker OTP is: %s. Valid for 5 minutes. Do not share with anyone.", otp
        );

        if (!enabled) {
            // Dev mode — just log it
            log.info("========================================");
            log.info("  [DEV] OTP for {}: {}", toPhone, otp);
            log.info("========================================");
            return;
        }

        try {
            Message.creator(
                    new PhoneNumber(toPhone),
                    new PhoneNumber(fromNumber),
                    messageBody
            ).create();
            log.info("OTP sent successfully to {}", toPhone);
        } catch (Exception e) {
            log.error("Failed to send OTP to {}: {}", toPhone, e.getMessage());
            throw new RuntimeException("Failed to send OTP. Please try again.");
        }
    }
}