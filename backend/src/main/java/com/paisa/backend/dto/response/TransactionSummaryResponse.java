package com.paisa.backend.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummaryResponse {
    private BigDecimal totalDebit;          // total money out
    private BigDecimal totalCredit;         // total money in
    private BigDecimal netBalance;          // credit - debit
    private long transactionCount;
    private Map<String, BigDecimal> spendByCategory;  // {"Food": 3200, "Travel": 1500}
    private Map<String, BigDecimal> spendBySource;    // {"UPI": 4500, "CARD": 2000}
}