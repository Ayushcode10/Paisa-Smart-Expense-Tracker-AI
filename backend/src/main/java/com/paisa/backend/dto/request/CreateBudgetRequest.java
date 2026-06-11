package com.paisa.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateBudgetRequest {

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "limit amount is required")
    @DecimalMin(value = "1.0", message = "Budget must be atleast Rs.1")
    private BigDecimal limitAmount;

    @NotBlank(message = "period is required")
    private String period;

    private boolean rollover;

    @Min(value = 1) @Max(value = 100)
    private Integer alertAt = 80;

}
