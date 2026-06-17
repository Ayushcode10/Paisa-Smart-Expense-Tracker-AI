package com.paisa.backend.dto.request;

import ch.qos.logback.core.joran.action.AppenderRefAction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateGoalRequest {
    @NotBlank(message = "Goal name is required")
    private String name;

    @NotNull(message = "target amount is required")
    @DecimalMin(value = "1.0", message = "Target amount must be at least Rs. 1")
    private BigDecimal targetAmount;

    @NotNull(message = "target date is required")
    @Future(message = "Target date must be in the future")
    private LocalDate targetDate;

    private BigDecimal currentAmount;
    private BigDecimal monthlyContribution;
    private String icon;

}
