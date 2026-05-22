package com.paisa.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_records")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtpRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String otp;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified")
    private boolean verified = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void createdAt(){
        createdAt = LocalDateTime.now();
    }

    public boolean isExpired(){
        return LocalDateTime.now().isAfter(expiresAt);
    }

}
