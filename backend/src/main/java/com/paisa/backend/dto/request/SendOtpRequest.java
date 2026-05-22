package com.paisa.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendOtpRequest {

    @NotBlank(message = "phone number is required")
    @Pattern(regexp = "^\\+91[6-9]\\d{9}$", message = "Enter a valid Indian mobile number with country code(+91) e.g. +919876543210")
    private String phone;
}
