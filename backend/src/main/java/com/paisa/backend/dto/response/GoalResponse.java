package com.paisa.backend.dto.response;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponse {

    private Long id;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal remainingAmount;
    private double percentComplete;
    private LocalDate targetDate;
    private int monthsRemaining;
    private BigDecimal requiredMonthlySaving;
    private boolean onTrack;
    private String icon;
    private String status;
    private LocalDateTime createdAt;
}
