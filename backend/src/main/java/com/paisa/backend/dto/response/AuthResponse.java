package com.paisa.backend.dto.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String phone;
    private String name;
    private boolean isNewUser;
    private String message;
}