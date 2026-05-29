package com.paisa.backend.service.sms.parser.hdfc;

import com.paisa.backend.service.sms.ParsedSmsResult;
import com.paisa.backend.service.sms.parser.BaseBankParser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HDFC Bank SMS Parser
 */
public class HdfcParser extends BaseBankParser {

    // =====================================================
    // AMOUNT PATTERNS
    // =====================================================

    private static final Pattern UPI_DEBIT_PATTERN =
            Pattern.compile(
                    "Sent\\s+Rs\\.?(\\d+\\.?\\d*)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern CREDIT_PATTERN =
            Pattern.compile(
                    "Rs\\.?(\\d+\\.?\\d*)\\s+credited",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern DEPOSITED_PATTERN =
            Pattern.compile(
                    "INR\\s+([0-9,]+(?:\\.\\d{2})?)\\s+deposited",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern CARD_SPEND_PATTERN =
            Pattern.compile(
                    "Spent\\s+Rs\\.?(\\d+\\.?\\d*)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern ATM_PATTERN =
            Pattern.compile(
                    "Rs\\.?(\\d+\\.?\\d*)\\s+withdrawn",
                    Pattern.CASE_INSENSITIVE
            );

    // =====================================================
    // ACCOUNT PATTERNS
    // =====================================================

    private static final Pattern ACCOUNT_PATTERN =
            Pattern.compile(
                    "(?:A/c|A/C)\\s+[*X]*(\\d{4})",
                    Pattern.CASE_INSENSITIVE
            );

    // =====================================================
    // BALANCE PATTERNS
    // =====================================================

    private static final Pattern BALANCE_PATTERN =
            Pattern.compile(
                    "(?:Avl|Avail|Available)\\s*Bal(?:ance)?[:\\s]*(?:INR|Rs\\.?)\\s*([\\d,]+\\.?\\d*)",
                    Pattern.CASE_INSENSITIVE
            );

    // =====================================================
    // MERCHANT PATTERNS
    // =====================================================

    private static final Pattern UPI_MERCHANT_PATTERN =
            Pattern.compile(
                    "To\\s+([A-Za-z ]+?)\\s+On",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern VPA_PATTERN =
            Pattern.compile(
                    "from\\s+VPA\\s+([a-zA-Z0-9.@_-]+)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern CARD_MERCHANT_PATTERN =
            Pattern.compile(
                    "At\\s+([A-Za-z0-9 .&_-]+?)\\s+On",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern ATM_LOCATION_PATTERN =
            Pattern.compile(
                    "At\\s+([A-Za-z ]+?)\\s+On",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern NEFT_PATTERN =
            Pattern.compile(
                    "NEFT\\s+Cr-[A-Z0-9]+-([A-Z\\s]+?)-[A-Z\\s]+-",
                    Pattern.CASE_INSENSITIVE
            );

    // =====================================================
    // MATCHER
    // =====================================================

    @Override
    public boolean matches(String sms) {

        sms = normalizeSms(sms);

        if (sms == null || sms.isBlank()) {
            return false;
        }

        String upper = sms.toUpperCase();

        return upper.contains("HDFC")
                || upper.contains("HDFCBK")
                || upper.contains("HDFCBANK");
    }

    // =====================================================
    // MAIN PARSER
    // =====================================================

    @Override
    public ParsedSmsResult parse(String sms) {

        sms = normalizeSms(sms);

        if (sms.isBlank()) {

            return ParsedSmsResult.failed(
                    "Empty HDFC SMS"
            );
        }

        BigDecimal amount =
                extractAmount(sms);

        if (amount == null) {

            return ParsedSmsResult.failed(
                    "Could not extract amount"
            );
        }

        String merchant =
                extractMerchant(sms);

        String account =
                extractAccount(sms);

        String type =
                detectTransactionType(sms);

        BigDecimal balance =
                extractBigDecimal(
                        BALANCE_PATTERN,
                        sms
                );

        return ParsedSmsResult.builder()
                .success(true)
                .amount(amount)
                .merchant(merchant)
                .accountLast4(account)
                .availableBalance(balance)
                .type(type)
                .source(detectSource(sms))
                .bankName("HDFC")
                .transactionDate(LocalDateTime.now())
                .rawSms(sms)
                .build();
    }

    // =====================================================
    // AMOUNT
    // =====================================================

    private BigDecimal extractAmount(String sms) {

        BigDecimal amount =
                extractBigDecimal(
                        UPI_DEBIT_PATTERN,
                        sms
                );

        if (amount != null) {
            return amount;
        }

        amount =
                extractBigDecimal(
                        CREDIT_PATTERN,
                        sms
                );

        if (amount != null) {
            return amount;
        }

        amount =
                extractBigDecimal(
                        DEPOSITED_PATTERN,
                        sms
                );

        if (amount != null) {
            return amount;
        }

        amount =
                extractBigDecimal(
                        CARD_SPEND_PATTERN,
                        sms
                );

        if (amount != null) {
            return amount;
        }

        return extractBigDecimal(
                ATM_PATTERN,
                sms
        );
    }

    // =====================================================
    // MERCHANT
    // =====================================================

    private String extractMerchant(String sms) {

        String lower = sms.toLowerCase();

        // =============================================
        // UPI DEBIT
        // =============================================

        String merchant =
                extractMatch(
                        UPI_MERCHANT_PATTERN,
                        sms
                );

        if (merchant != null) {

            merchant =
                    cleanMerchant(merchant);

            if (!merchant.isBlank()) {
                return merchant;
            }
        }

        // =============================================
        // VPA CREDIT
        // =============================================

        merchant =
                extractMatch(
                        VPA_PATTERN,
                        sms
                );

        if (merchant != null) {

            merchant =
                    cleanMerchant(merchant);

            if (!merchant.isBlank()) {
                return merchant;
            }
        }

        // =============================================
        // CARD SPEND
        // =============================================

        merchant =
                extractMatch(
                        CARD_MERCHANT_PATTERN,
                        sms
                );

        if (merchant != null) {

            merchant =
                    cleanMerchant(merchant);

            if (!merchant.isBlank()) {
                return merchant;
            }
        }

        // =============================================
        // ATM
        // =============================================

        if (lower.contains("withdrawn")) {

            merchant =
                    extractMatch(
                            ATM_LOCATION_PATTERN,
                            sms
                    );

            if (merchant != null) {

                return "ATM " +
                        cleanMerchant(merchant);
            }

            return "ATM Withdrawal";
        }

        // =============================================
        // NEFT
        // =============================================

        merchant =
                extractMatch(
                        NEFT_PATTERN,
                        sms
                );

        if (merchant != null) {

            merchant =
                    cleanMerchant(merchant);

            if (!merchant.isBlank()) {
                return merchant;
            }
        }

        return "HDFC Transaction";
    }

    // =====================================================
    // ACCOUNT
    // =====================================================

    private String extractAccount(String sms) {

        String account =
                extractMatch(
                        ACCOUNT_PATTERN,
                        sms
                );

        if (account == null) {
            return null;
        }

        String digits =
                account.replaceAll("\\D", "");

        if (digits.length() < 4) {
            return null;
        }

        return digits.substring(
                digits.length() - 4
        );
    }

    // =====================================================
    // TYPE
    // =====================================================

    private String detectTransactionType(String sms) {

        String lower = sms.toLowerCase();

        if (lower.contains("credited")) {
            return "CREDIT";
        }

        if (lower.contains("deposited")) {
            return "CREDIT";
        }

        if (lower.contains("sent")) {
            return "DEBIT";
        }

        if (lower.contains("spent")) {
            return "DEBIT";
        }

        if (lower.contains("withdrawn")) {
            return "DEBIT";
        }

        return "UNKNOWN";
    }

    // =====================================================
    // SOURCE
    // =====================================================

    private String detectSource(String sms) {

        String upper = sms.toUpperCase();

        if (upper.contains("UPI")) {
            return "UPI";
        }

        if (upper.contains("VPA")) {
            return "UPI";
        }

        if (upper.contains("CARD")) {
            return "CARD";
        }

        if (upper.contains("ATM")) {
            return "ATM";
        }

        if (upper.contains("NEFT")) {
            return "BANK";
        }

        return "BANK";
    }
}