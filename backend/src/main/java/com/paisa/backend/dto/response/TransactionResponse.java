package com.paisa.backend.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// This is what the frontend receives — clean and intentional
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String id;
    private BigDecimal amount;
    private String merchant;
    private String category;
    private String type;
    private String source;
    private String paymentMethod;
    private String note;
    private List<String> tags;
    private boolean smsImported;
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;
}