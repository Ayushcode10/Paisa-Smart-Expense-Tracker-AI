package com.paisa.backend.service.sms.parser.axis;

import com.paisa.backend.service.sms.ParsedSmsResult;
import com.paisa.backend.service.sms.parser.BaseBankParser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Axis Bank SMS Parser
 */
public class AxisBankParser extends BaseBankParser {

    // =====================================================
    // AMOUNT PATTERNS
    // =====================================================


    private static final Pattern SPENT_AMOUNT_PATTERN =
            Pattern.compile(
                    "Spent.*?INR\\s+([0-9,]+(?:\\.\\d{2})?)",
                    Pattern.CASE_INSENSITIVE
            );
    private static final Pattern INR_DEBIT_PATTERN =
            Pattern.compile(
                    "INR\\s+([0-9,]+(?:\\.\\d{2})?)\\s+debited",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern INR_CREDIT_PATTERN =
            Pattern.compile(
                    "INR\\s+([0-9,]+(?:\\.\\d{2})?)\\s+credited",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern PAYMENT_PATTERN =
            Pattern.compile(
                    "Payment\\s+of\\s+INR\\s+([0-9,]+(?:\\.\\d{2})?)",
                    Pattern.CASE_INSENSITIVE
            );

    // =====================================================
    // ACCOUNT PATTERNS
    // =====================================================

    private static final Pattern ACCOUNT_PATTERN =
            Pattern.compile(
                    "A/c\\s+no\\.\\s+([X*x\\d]+)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern CARD_PATTERN =
            Pattern.compile(
                    "Card\\s+no\\.\\s+([X*x\\d]+)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern CREDIT_CARD_PATTERN =
            Pattern.compile(
                    "Credit\\s+Card\\s+([X*x\\d]+)",
                    Pattern.CASE_INSENSITIVE
            );

    // =====================================================
    // AVAILABLE LIMIT
    // =====================================================

    private static final Pattern AVL_LIMIT_PATTERN =
            Pattern.compile(
                    "Avl\\s+Limit:?\\s*INR\\s+([0-9,]+(?:\\.\\d{2})?)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern AVL_LMT_PATTERN =
            Pattern.compile(
                    "Avl\\s+Lmt\\s+INR\\s+([0-9,]+(?:\\.\\d{2})?)",
                    Pattern.CASE_INSENSITIVE
            );

    // =====================================================
    // BALANCE
    // =====================================================

    private static final Pattern BALANCE_PATTERN =
            Pattern.compile(
                    "Avl\\s+bal:?\\s+INR\\s+([0-9,]+(?:\\.\\d{2})?)",
                    Pattern.CASE_INSENSITIVE
            );

    // =====================================================
    // REFERENCE
    // =====================================================

    private static final Pattern UPI_REF_PATTERN =
            Pattern.compile(
                    "UPI/[^/]+/([0-9]+)",
                    Pattern.CASE_INSENSITIVE
            );

    // =====================================================
    // MERCHANT PATTERNS
    // =====================================================

    private static final Pattern DEBIT_CARD_PATTERN =
            Pattern.compile(
                    "debited from A/c no\\. [^\\s]+ on ([^0-9]+?)(?:\\d{2}-\\d{2}-\\d{4})",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern UPI_MERCHANT_PATTERN =
            Pattern.compile(
                    "UPI/[^/]+/[^/]+/([^\\n]+?)(?:\\s*Not you|\\s*$)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern UPI_PERSON_PATTERN =
            Pattern.compile(
                    "UPI/P2A/[^/]+/([^\\n]+?)(?:\\s*Not you|\\s*$)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern INFO_PATTERN =
            Pattern.compile(
                    "Info\\s*[-–]\\s*([^.\\n]+?)(?:\\.\\s*Chk|\\s*$)",
                    Pattern.CASE_INSENSITIVE
            );

    // =====================================================
    // CREDIT CARD MULTILINE PATTERNS
    // =====================================================

    private static final Pattern CREDIT_CARD_SPENT_PATTERN =
            Pattern.compile(
                    "Spent(?:\\s+INR)?\\s+[0-9,.]+.*?(?:IST\\s+)?([A-Z][A-Z\\s]+?)(?:\\s+Avl\\s+Limit|\\s+Avl\\s+Lmt|\\s+Not\\s+you)",
                    Pattern.CASE_INSENSITIVE
            );

    // =====================================================
    // MATCHES
    // =====================================================

    @Override
    public boolean matches(String sms) {

        sms = normalizeSms(sms);

        if (sms.isBlank()) {
            return false;
        }

        String upper = sms.toUpperCase();

        return upper.contains("AXIS BANK")
                || upper.contains("AXISBANK")
                || upper.contains("AXISBK")
                || upper.contains("AXISB")
                || upper.contains("AXIS");
    }

    // =====================================================
    // MAIN PARSER
    // =====================================================

    @Override
    public ParsedSmsResult parse(String sms) {

        sms = normalizeSms(sms);

        if (sms.isBlank()) {

            return ParsedSmsResult.failed(
                    "Empty Axis SMS"
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

        String account = extractAccount(sms);

        BigDecimal availableLimit =
                extractAvailableLimit(sms);

        BigDecimal balance =
                extractBigDecimal(
                        BALANCE_PATTERN,
                        sms
                );

        String reference =
                extractMatch(
                        UPI_REF_PATTERN,
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
                .bankName("Axis Bank")
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
                        INR_DEBIT_PATTERN,
                        sms
                );

        if (amount != null) {
            return amount;
        }

        amount =
                extractBigDecimal(
                        INR_CREDIT_PATTERN,
                        sms
                );

        if (amount != null) {
            return amount;
        }

        amount =
                extractBigDecimal(
                        PAYMENT_PATTERN,
                        sms
                );

        if (amount != null) {
            return amount;
        }

        return extractBigDecimal(
                SPENT_AMOUNT_PATTERN,
                sms
        );
    }

    // =====================================================
    // MERCHANT
    // =====================================================

    private String extractMerchant(String sms) {

        String lower = sms.toLowerCase();

        // =================================================
        // ATM
        // =================================================

        if (lower.contains("debited from a/c no.")
                && lower.contains(" on axis bank")) {

            return "ATM";
        }

        if ((lower.contains("atm")
                || lower.contains("cash withdrawal"))
                && lower.contains("debited")) {

            return "ATM";
        }

        // =================================================
        // DEBIT CARD
        // =================================================

        String merchant =
                extractMatch(
                        DEBIT_CARD_PATTERN,
                        sms
                );

        if (merchant != null) {

            merchant = cleanMerchant(merchant);

            if (!merchant.isBlank()) {
                return merchant;
            }
        }

        // =================================================
        // CREDIT CARD WITH IST
        // =================================================

        Matcher spentMatcher =
                CREDIT_CARD_PATTERN.matcher(sms);

        if (spentMatcher.find()) {

            merchant =
                    spentMatcher.group(1).trim();

            merchant =
                    merchant.replaceAll("\\s+Limi$", "")
                            .replaceAll("\\s+Pay$", "")
                            .replaceAll("\\s+SUPE$", "");

            merchant = cleanMerchant(merchant);

            if (!merchant.isBlank()) {
                return merchant;
            }
        }

        // =================================================
        // CREDIT CARD WITHOUT IST
        // =================================================

        Matcher spentMatcher2 =
                CREDIT_CARD_PATTERN.matcher(sms);

        if (spentMatcher2.find()) {

            merchant =
                    spentMatcher2.group(1).trim();

            merchant =
                    merchant.replaceAll("\\s+Limi$", "")
                            .replaceAll("\\s+Pay$", "")
                            .replaceAll("\\s+SUPE$", "");

            merchant = cleanMerchant(merchant);

            if (!merchant.isBlank()) {
                return merchant;
            }
        }

        // =================================================
        // UPI MERCHANT
        // =================================================

        merchant =
                extractMatch(
                        UPI_MERCHANT_PATTERN,
                        sms
                );

        if (merchant != null) {

            merchant = cleanMerchant(merchant);

            if (!merchant.isBlank()) {
                return merchant;
            }
        }

        // =================================================
        // UPI PERSON
        // =================================================

        merchant =
                extractMatch(
                        UPI_PERSON_PATTERN,
                        sms
                );

        if (merchant != null) {

            merchant = cleanMerchant(merchant);

            if (!merchant.isBlank()) {
                return merchant;
            }
        }

        // =================================================
        // INFO PATTERN
        // =================================================

        merchant =
                extractMatch(
                        INFO_PATTERN,
                        sms
                );

        if (merchant != null) {

            if (merchant.toUpperCase()
                    .contains("SALARY")) {

                return "Salary";
            }

            return cleanMerchant(merchant);
        }

        return "Axis Transaction";
    }

    // =====================================================
    // ACCOUNT
    // =====================================================

    private String extractAccount(String sms) {

        String raw =
                extractMatch(
                        ACCOUNT_PATTERN,
                        sms
                );

        if (raw == null) {

            raw =
                    extractMatch(
                            CARD_PATTERN,
                            sms
                    );
        }

        if (raw == null) {

            raw =
                    extractMatch(
                            CREDIT_CARD_PATTERN,
                            sms
                    );
        }

        if (raw == null) {
            return null;
        }

        String digits =
                raw.replaceAll("\\D", "");

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

        // Credit card
        if (lower.contains("avl limit")
                || lower.contains("avl lmt")) {

            return "CREDIT";
        }

        if ((lower.contains("credit card")
                || lower.contains(" cc "))
                && (lower.contains("debited")
                || lower.contains("spent"))) {

            return "CREDIT";
        }

        // Credit
        if (lower.contains("credited")) {
            return "CREDIT";
        }

        // Debit
        if (lower.contains("debited")
                || lower.contains("spent")) {

            return "DEBIT";
        }

        return "UNKNOWN";
    }

    // =====================================================
    // AVAILABLE LIMIT
    // =====================================================

    private BigDecimal extractAvailableLimit(
            String sms
    ) {

        BigDecimal limit =
                extractBigDecimal(
                        AVL_LIMIT_PATTERN,
                        sms
                );

        if (limit != null) {
            return limit;
        }

        return extractBigDecimal(
                AVL_LMT_PATTERN,
                sms
        );
    }

    // =====================================================
    // SOURCE
    // =====================================================

    private String detectSource(String sms) {

        String upper = sms.toUpperCase();

        if (upper.contains("UPI")) {
            return "UPI";
        }

        if (upper.contains("CARD")) {
            return "CARD";
        }

        if (upper.contains("ATM")) {
            return "ATM";
        }

        return "BANK";
    }
}