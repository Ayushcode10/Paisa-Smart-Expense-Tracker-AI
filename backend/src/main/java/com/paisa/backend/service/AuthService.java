package com.paisa.backend.service;

import com.paisa.backend.dto.request.SendOtpRequest;
import com.paisa.backend.dto.request.VerifyOtpRequest;
import com.paisa.backend.dto.response.AuthResponse;
import com.paisa.backend.entity.OtpRecord;
import com.paisa.backend.entity.User;
import com.paisa.backend.repository.mysql.OtpRepository;
import com.paisa.backend.repository.mysql.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final SmsService smsService;

    @Value("${app.otp.expiry-minutes}")
    private int otpExpiryMinutes;

    @Value("${app.otp.length}")
    private int otpLength;

    public String sendOtp(SendOtpRequest request) {
        String phone = request.getPhone();

        // Delete any existing OTPs for this phone
        otpRepository.deleteAllByPhone(phone);

        // Generate new OTP
        String otp = generateOtp();

        // Save to DB
        OtpRecord record = OtpRecord.builder()
                .phone(phone)
                .otp(otp)
                .verified(false)
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .build();
        otpRepository.save(record);

        // Send via SMS
        smsService.sendOtp(phone, otp);

        return "OTP sent successfully to " + phone;
    }

    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        String phone = request.getPhone();
        String otp = request.getOtp();

        // Find latest OTP record
        OtpRecord record = otpRepository
                .findTopByPhoneAndVerifiedFalseOrderByCreatedAtDesc(phone)
                .orElseThrow(() -> new RuntimeException("OTP not found. Please request a new one."));

        // Check expiry
        if (record.isExpired()) {
            otpRepository.delete(record);
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        // Check OTP match
        if (!record.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP. Please try again.");
        }

        // Mark as verified
        record.setVerified(true);
        otpRepository.save(record);

        // Find or create user
        Optional<User> existingUser = userRepository.findByPhone(phone);
        boolean isNewUser = existingUser.isEmpty();

        User user = existingUser.orElseGet(() -> {
            User newUser = User.builder()
                    .phone(phone)
                    .name(request.getName() != null ? request.getName() : "")
                    .build();
            return userRepository.save(newUser);
        });

        // Generate JWT
        String token = jwtService.generateToken(phone, user.getId());

        return AuthResponse.builder()
                .token(token)
                .phone(user.getPhone())
                .name(user.getName())
                .isNewUser(isNewUser)
                .message(isNewUser ? "Welcome to ExpenseTracker!" : "Welcome back!")
                .build();
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}