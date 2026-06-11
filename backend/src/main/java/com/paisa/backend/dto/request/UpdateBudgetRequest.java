package com.paisa.backend.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateBudgetRequest {
    private BigDecimal limitAmount;
    private Integer alertAt;
    private Boolean rollover;
}
