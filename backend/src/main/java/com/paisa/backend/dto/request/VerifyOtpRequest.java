package com.paisa.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Setter;

@Data
public class VerifyOtpRequest {
    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Otp is required")
    @Size(min = 6, max = 6, message = "Otp must be 6 digits")
    private String otp;

    private String name; // optional - for new users on first login
}
