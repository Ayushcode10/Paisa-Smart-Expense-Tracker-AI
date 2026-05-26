package com.paisa.backend.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.engine.jdbc.env.internal.BlobAndClobCreator;
import org.springframework.core.SpringVersion;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = true)
    private String phone;

    private String name;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
    }
}
