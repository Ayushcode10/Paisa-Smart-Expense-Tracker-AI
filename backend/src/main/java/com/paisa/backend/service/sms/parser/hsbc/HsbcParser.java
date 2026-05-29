package com.paisa.backend.service.sms.parser.hsbc;

import com.paisa.backend.service.sms.ParsedSmsResult;
import com.paisa.backend.service.sms.parser.BaseBankParser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class HsbcParser extends BaseBankParser {

    // =====================================================
    // CURRENCY
    // =====================================================

    private static final String CUR =
            "(?:INR|EGP|USD|GBP|EUR|AED|SAR|OMR|BHD|KWD|QAR)";

    // =====================================================
    // PATTERNS
    // =====================================================

    // Amount patterns
    private static final Pattern AMOUNT_PATTERN_1 =
            Pattern.compile(
                    CUR + "\\s+([\\d,]+(?:\\.\\d+)?)\\s+is\\s+(?:paid|credited|debited)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern AMOUNT_PATTERN_2 =
            Pattern.compile(
                    "for\\s+" + CUR + "\\s+([\\d,]+(?:\\.\\d+)?)",
                    Pattern.CASE_INSENSITIVE
            );

    // Merchant patterns
    private static final Pattern AT_PATTERN =
            Pattern.compile(
                    "at\\s+(.+?)\\s+for\\s+" + CUR,
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern TO_PATTERN =
            Pattern.compile(
                    "to\\s+([^.]+?)\\s+on",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern FROM_PATTERN =
            Pattern.compile(
                    "from\\s+([^.]+?)(?:\\s+on|\\s+with|$)",
                    Pattern.CASE_INSENSITIVE
            );

    // Account
    private static final Pattern ACCOUNT_PATTERN =
            Pattern.compile(
                    "A/c\\s+([\\d\\-*X]+)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern CREDIT_CARD_PATTERN =
            Pattern.compile(
                    "creditcard\\s+([xX*\\d]+)",
                    Pattern.CASE_INSENSITIVE
            );

    // Balance
    private static final Pattern BALANCE_PATTERN =
            Pattern.compile(
                    "Avl\\s+Bal\\s+is\\s+" + CUR + "\\s+([\\d,]+(?:\\.\\d+)?)",
                    Pattern.CASE_INSENSITIVE
            );

    // Available limit
    private static final Pattern LIMIT_PATTERN =
            Pattern.compile(
                    "available\\s+limit\\s+is\\s+" + CUR + "\\s+([\\d,]+(?:\\.\\d+)?)",
                    Pattern.CASE_INSENSITIVE
            );

    // Reference
    private static final Pattern REF_PATTERN =
            Pattern.compile(
                    "with\\s+(?:UTR|ref)\\s+(\\w+)",
                    Pattern.CASE_INSENSITIVE
            );

    // =====================================================
    // MATCHER
    // =====================================================

    @Override
    public boolean matches(String sms) {

        sms = normalizeSms(sms);

        if (sms.isBlank()) {
            return false;
        }

        String upper = sms.toUpperCase();

        return upper.contains("HSBC")
                || upper.contains("HSBCIN");
    }

    // =====================================================
    // MAIN PARSER
    // =====================================================

    @Override
    public ParsedSmsResult parse(String sms) {

        sms = normalizeSms(sms);

        if (sms.isBlank()) {

            return ParsedSmsResult.failed(
                    "Empty HSBC SMS"
            );
        }

        BigDecimal amount = extractAmount(sms);

        if (amount == null) {

            return ParsedSmsResult.failed(
                    "Could not extract amount"
            );
        }

        String merchant = extractMerchant(sms);

        String type = detectTransactionType(sms);

        String rawAccount =
                        extractMatch(
                                ACCOUNT_PATTERN,
                                sms
                        );
        if (rawAccount == null) {

            rawAccount =
                    extractMatch(
                            CREDIT_CARD_PATTERN,
                            sms
                    );
        }

        String account =
                extractLast4Digits(rawAccount);



        BigDecimal balance =
                extractBigDecimal(
                        BALANCE_PATTERN,
                        sms
                );

        BigDecimal limit =
                extractBigDecimal(
                        LIMIT_PATTERN,
                        sms
                );

        String reference =
                extractMatch(
                        REF_PATTERN,
                        sms
                );

        return ParsedSmsResult.builder()
                .success(true)
                .amount(amount)
                .merchant(merchant)
                .type(type)
                .accountLast4(account)
                .availableBalance(balance)
                .source(detectSource(sms))
                .bankName("HSBC")
                .transactionDate(LocalDateTime.now())
                .rawSms(sms)
                .build();
    }

    // =====================================================
    // EXTRACT AMOUNT
    // =====================================================

    private BigDecimal extractAmount(String sms) {

        BigDecimal amount =
                extractBigDecimal(
                        AMOUNT_PATTERN_1,
                        sms
                );

        if (amount != null) {
            return amount;
        }

        return extractBigDecimal(
                AMOUNT_PATTERN_2,
                sms
        );
    }

    // =====================================================
    // EXTRACT MERCHANT
    // =====================================================

    private String extractMerchant(String sms) {

        String merchant =
                extractMatch(
                        AT_PATTERN,
                        sms
                );

        if (merchant != null) {
            return cleanMerchant(merchant);
        }

        merchant =
                extractMatch(
                        TO_PATTERN,
                        sms
                );

        if (merchant != null) {
            return cleanMerchant(merchant);
        }

        merchant =
                extractMatch(
                        FROM_PATTERN,
                        sms
                );

        if (merchant != null) {
            return cleanMerchant(merchant);
        }

        return "HSBC Transaction";
    }

    // =====================================================
    // DETECT TYPE
    // =====================================================

    private String detectTransactionType(String sms) {

        String lower = sms.toLowerCase();

        // Debit card
        if (lower.contains("debit card")
                || lower.contains("paid")
                || lower.contains("debited")
                || lower.contains("used at")) {

            return "DEBIT";
        }

        // Credit card
        if (lower.contains("credit card")
                || lower.contains("creditcard")) {

            return "CREDIT_CARD";
        }

        // Income
        if (lower.contains("credited")
                || lower.contains("deposited")) {

            return "CREDIT";
        }

        // Transfer
        if (lower.contains("neft")
                || lower.contains("rtgs")
                || lower.contains("imps")) {

            return "TRANSFER";
        }

        return "UNKNOWN";
    }

    // =====================================================
    // DETECT SOURCE
    // =====================================================

    private String detectSource(String sms) {

        String upper = sms.toUpperCase();

        if (upper.contains("UPI")) {
            return "UPI";
        }

        if (upper.contains("CARD")) {
            return "CARD";
        }

        if (upper.contains("NEFT")
                || upper.contains("RTGS")
                || upper.contains("IMPS")) {

            return "BANK_TRANSFER";
        }

        return "BANK";
    }

    // =====================================================
    // EXTRACT LAST 4 DIGITS
    // =====================================================

    private String extractLast4Digits(String raw) {

        if (raw == null) {
            return null;
        }

        String digits =
                raw.replaceAll("\\D", "");

        if (digits.length() >= 4) {

            return digits.substring(
                    digits.length() - 4
            );
        }

        return digits;
    }
}