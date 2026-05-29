package com.paisa.backend.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "transactions")  // → this is the MongoDB collection name
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    private String id;                  // MongoDB auto-generates this (like "64f3a2b1c9e...")

    @Indexed                            // index on userId so queries are fast
    private Long userId;                // which user does this belong to (links to MySQL)

    private BigDecimal amount;          // always use BigDecimal for money, never double

    private String merchant;            // "Zomato", "Amazon", "HDFC ATM"
    private String merchantNormalized;  // cleaned up version → "zomato" (lowercase, no symbols)

    private String category;            // "Food", "Travel", "Shopping", "Bills", etc.
    private Double categoryConfidence;  // 0.0 to 1.0 — how confident AI was in the category

    private String type;                // "DEBIT" or "CREDIT"
    private String source;              // "UPI", "CARD", "CASH", "WALLET", "NET_BANKING"
    private String paymentMethod;       // "GPay", "PhonePe", "HDFC Credit Card"

    private String bankName;       // "HDFC", "SBI", "ICICI"
    private String accountLast4;   // "XX4521"

    private String note;                // user's own note on the transaction
    private List<String> tags;          // ["work", "reimbursable"] — user defined

    private String rawSms;              // original SMS text — useful for debugging
    private boolean smsImported;        // was this from SMS or manually entered?

    private String currency;            // "INR" (default)

    @Indexed
    private LocalDateTime transactionDate;  // when the actual transaction happened
    private LocalDateTime createdAt;        // when we saved it in our system
    private LocalDateTime updatedAt;
}