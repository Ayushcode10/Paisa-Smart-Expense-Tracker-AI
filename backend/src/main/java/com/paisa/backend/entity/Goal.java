package com.paisa.backend.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false)
    private String name;                    //goa trip

    @Column(name = "target_amount", nullable = false)
    private BigDecimal targetAmount;        //50,000

    @Column(name = "current_amount", nullable = false)
    private BigDecimal currentAmount = BigDecimal.ZERO;     //how much saved so far

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "monthly_contribution", nullable = false)
    private BigDecimal monthlyContribution;

    private String icon;

    @Column(nullable = false)
    private String status = "ACTIVE";   //"ACTIVE","COMPLETED","ABANDONED";

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if(currentAmount == null) currentAmount = BigDecimal.ZERO;
    }
    @PreUpdate
    protected void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }
}
