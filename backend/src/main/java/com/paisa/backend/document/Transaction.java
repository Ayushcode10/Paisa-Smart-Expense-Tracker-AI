package com.paisa.backend.document;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {

    @Id
    private String id;             // MongoDB uses String IDs

    private Long userId;           // FK to MySQL user
    private BigDecimal amount;
    private String merchant;
    private String category;
    private String type;           // DEBIT / CREDIT
    private String source;         // UPI / CARD / WALLET
    private String rawSms;         // original SMS if parsed
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;
}
