package com.paisa.backend.service.sms.parser.hdfc;

import com.paisa.backend.service.sms.ParsedSmsResult;
import com.paisa.backend.service.sms.parser.BaseBankParser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class HdfcMf extends BaseBankParser {

    // =====================================================
    // PATTERNS
    // =====================================================

    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile(
                    "Rs\\.?\\s*([\\d,]+\\.?\\d*)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern FUND_PATTERN =
            Pattern.compile(
                    "under\\s+(.+?)\\s+for",
                    Pattern.CASE_INSENSITIVE
            );

    // =====================================================
    // MATCHER
    // =====================================================

    @Override
    public boolean matches(String sms) {

        sms = normalizeSms(sms);
        if (sms == null) {
            return false;
        }

        String upper = sms.toUpperCase();

        return upper.contains("HDFCMF")
                || upper.contains("HDFC MUTUAL FUND")
                || upper.contains("SIP PURCHASE")
                || upper.contains("FOLIO");
    }

    // =====================================================
    // MAIN PARSER
    // =====================================================

    @Override
    public ParsedSmsResult parse(String sms) {
        sms = normalizeSms(sms);
        BigDecimal amount =
                extractBigDecimal(
                        AMOUNT_PATTERN,
                        sms
                );

        if (amount == null) {

            return ParsedSmsResult.failed(
                    "Could not extract amount"
            );
        }

        String fundName =
                extractMatch(
                        FUND_PATTERN,
                        sms
                );

        String merchant =
                fundName != null
                        ? cleanMerchant(fundName)
                        : "HDFC Mutual Fund";

        String type = detectTransactionType(sms);

        return ParsedSmsResult.builder()
                .success(true)
                .amount(amount)
                .merchant(merchant)
                .type(type)
                .source("MUTUAL_FUND")
                .bankName("HDFC Mutual Fund")
                .accountLast4("")
                .availableBalance(null)
                .transactionDate(LocalDateTime.now())
                .rawSms(sms)
                .build();
    }

    // =====================================================
    // TRANSACTION TYPE
    // =====================================================

    private String detectTransactionType(String sms) {

        String lower = sms.toLowerCase();

        if (lower.contains("sip purchase")
                || lower.contains("purchase")) {

            return "INVESTMENT";
        }

        if (lower.contains("redemption")) {

            return "INCOME";
        }

        return "UNKNOWN";
    }
}