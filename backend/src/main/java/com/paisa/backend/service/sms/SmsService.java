package com.paisa.backend.service.sms;

import com.paisa.backend.config.AuthUtils;
import com.paisa.backend.document.Transaction;
import com.paisa.backend.dto.response.TransactionResponse;
import com.paisa.backend.repository.mongo.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final SmsParser smsParser;
    private final TransactionRepository transactionRepository;

    // ─────────────────────────────────────────────
    // Parse ONE SMS and save it
    // Called when user grants permission and app
    // sends new incoming SMS in real time
    // ─────────────────────────────────────────────
    public ParsedSmsResult parseAndSave(String smsText) {
        Long userId = AuthUtils.getCurrentUserId();

        ParsedSmsResult result = smsParser.parse(smsText);

        if (!result.isSuccess()) {
            log.info("SMS parsing failed: {}", result.getFailureReason());
            return result;  // return failure — don't save anything
        }

        // Build and save the transaction
        Transaction transaction = buildTransaction(result, userId);
        transactionRepository.save(transaction);

        log.info("SMS parsed and saved as transaction for user {}: {} {}",
                userId, result.getType(), result.getAmount());

        return result;
    }

    // ─────────────────────────────────────────────
    // Parse BULK SMS — called on first app launch
    // Android reads last 6 months of SMS and sends
    // them all at once for historical import
    // ─────────────────────────────────────────────
    public BulkSmsResult parseAndSaveBulk(List<String> smsList) {
        Long userId = AuthUtils.getCurrentUserId();

        int successCount = 0;
        int failureCount = 0;
        List<String> failedSms = new ArrayList<>();

        for (String smsText : smsList) {
            ParsedSmsResult result = smsParser.parse(smsText);

            if (result.isSuccess()) {
                Transaction transaction = buildTransaction(result, userId);
                transactionRepository.save(transaction);
                successCount++;
            } else {
                failureCount++;
                failedSms.add(smsText.substring(0, Math.min(50, smsText.length()))); // first 50 chars for logging
            }
        }

        log.info("Bulk SMS import for user {}: {} success, {} failed",
                userId, successCount, failureCount);

        return BulkSmsResult.builder()
                .totalReceived(smsList.size())
                .successCount(successCount)
                .failureCount(failureCount)
                .failedSmsPreview(failedSms)
                .build();
    }

    // ─────────────────────────────────────────────
    // Just parse — don't save (useful for testing)
    // Your frontend partner can use this to show
    // "we found this in your SMS, is this correct?"
    // before actually saving
    // ─────────────────────────────────────────────
    public ParsedSmsResult previewParse(String smsText) {
        return smsParser.parse(smsText);
    }

    // ─────────────────────────────────────────────
    // Converts ParsedSmsResult → Transaction document
    // ─────────────────────────────────────────────
    private Transaction buildTransaction(ParsedSmsResult result, Long userId) {
        return Transaction.builder()
                .userId(userId)
                .amount(result.getAmount())
                .type(result.getType())
                .merchant(result.getMerchant())
                .merchantNormalized(result.getMerchant().toLowerCase().split(" ")[0])
                .category("Uncategorized")   // AI will categorize later
                .source(result.getSource())
                .bankName(result.getBankName())
                .accountLast4(result.getAccountLast4())
                .rawSms(result.getRawSms())
                .smsImported(true)           // flag that this came from SMS
                .currency("INR")
                .transactionDate(result.getTransactionDate() != null
                        ? result.getTransactionDate()
                        : LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}