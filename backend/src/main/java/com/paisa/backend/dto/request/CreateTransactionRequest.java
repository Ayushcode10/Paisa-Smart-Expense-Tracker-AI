package com.paisa.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateTransactionRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "merchant name is required")
    private String merchant;

    @NotBlank(message = "type is required")
    private String type;

    private String category;        //optional --- AI will assign if not provided
    private String source;          // upi, card, cash, wallet
    private String paymentMethod;
    private String note;
    private List<String> tags;

//    if not provided we default to now();
    private LocalDateTime transactionDate;
}
