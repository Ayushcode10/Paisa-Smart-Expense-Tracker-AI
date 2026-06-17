package com.paisa.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ContributeGoalRequest {

    @NotNull
    @DecimalMin(value = "1.0", message = "Contribution must be at least Rs 1")
    private BigDecimal amount;

    private String note;
}
