package com.paisa.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "budgets",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","category","period"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String category;            //food, travel ??

    @Column(name = "limit_amount", nullable = false)
    private BigDecimal limitAmount;      // $5000

    @Column(nullable = false)
    private String period;                //monthly or weekly

    @Column(name = "rollover")
    private boolean rollover = false;     //carry unused budget forward?

    @Column(name = "alert_at")
    private Integer alertAt = 80;           //send alert at 80% consumed

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
    }
}
