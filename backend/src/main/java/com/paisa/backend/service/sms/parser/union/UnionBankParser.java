package com.paisa.backend.service.sms.parser.union;

import com.paisa.backend.service.sms.ParsedSmsResult;
import com.paisa.backend.service.sms.parser.BaseBankParser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Union Bank Parser
 *
 * Supports:
 * - Debit
 * - Credit
 * - ATM
 * - UPI
 * - Mobile Banking
 */
public class UnionBankParser extends BaseBankParser {

    // =====================================================
    // PATTERNS
    // =====================================================

    // Amount patterns
    private static final Pattern RS_AMOUNT_PATTERN =
            Pattern.compile(
                    "Rs[:.]?\\s*([0-9,]+(?:\\.\\d{2})?)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern INR_AMOUNT_PATTERN =
            Pattern.compile(
                    "INR\\s+([0-9,]+(?:\\.\\d{2})?)",
                    Pattern.CASE_INSENSITIVE
            );

    // Account
    private static final Pattern ACCOUNT_PATTERN =
            Pattern.compile(
                    "A/[Cc]\\s*[*X]?(\\d{4})",
                    Pattern.CASE_INSENSITIVE
            );

    // Balance
    private static final Pattern BALANCE_PATTERN =
            Pattern.compile(
                    "Avl\\s+Bal\\s+Rs[:.]?\\s*([0-9,]+(?:\\.\\d{2})?)",
                    Pattern.CASE_INSENSITIVE
            );

    // Reference
    private static final Pattern REF_PATTERN =
            Pattern.compile(
                    "ref\\s+no\\s+([\\w]+)",
                    Pattern.CASE_INSENSITIVE
            );

    // Merchant after "to"
    private static final Pattern TO_PATTERN =
            Pattern.compile(
                    "to\\s+([^.\n]+?)(?:\\s+on|\\s+Avl|$)",
                    Pattern.CASE_INSENSITIVE
            );

    // Merchant after "from"
    private static final Pattern FROM_PATTERN =
            Pattern.compile(
                    "from\\s+([^.\n]+?)(?:\\s+on|\\s+Avl|$)",
                    Pattern.CASE_INSENSITIVE
            );

    // ATM location
    private static final Pattern ATM_PATTERN =
            Pattern.compile(
                    "at\\s+([^.\n]+?)(?:\\s+on|\\s+Avl|$)",
                    Pattern.CASE_INSENSITIVE
            );

    // UPI
    private static final Pattern UPI_PATTERN =
            Pattern.compile(
                    "UPI[/:]?\\s*([^,.\\s]+)",
                    Pattern.CASE_INSENSITIVE
            );

    // VPA
    private static final Pattern VPA_PATTERN =
            Pattern.compile(
                    "VPA\\s+([^@\\s]+)",
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
        System.out.println("Normalized SMS: " + upper);
        return upper.contains("UNIONB")
                || upper.contains("UNIONBANK")
                || upper.contains("UNION BANK")
                || upper.contains("UBOI");
    }

    // =====================================================
    // MAIN PARSER
    // =====================================================

    @Override
    public ParsedSmsResult parse(String sms) {

        sms = normalizeSms(sms);
        if (sms == null || sms.isBlank()) {

            return ParsedSmsResult.failed(
                    "Empty Union Bank SMS"
            );
        }

        BigDecimal amount = extractAmount(sms);

        if (amount == null) {

            return ParsedSmsResult.failed(
                    "Could not extract amount"
            );
        }

        String type = detectTransactionType(sms);

        String merchant = extractMerchant(sms);

        String account =
                extractMatch(
                        ACCOUNT_PATTERN,
                        sms
                );

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
                .bankName("Union Bank")
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
                        RS_AMOUNT_PATTERN,
                        sms
                );

        if (amount != null) {
            return amount;
        }

        return extractBigDecimal(
                INR_AMOUNT_PATTERN,
                sms
        );
    }

    // =====================================================
    // DETECT TYPE
    // =====================================================

    private String detectTransactionType(String sms) {

        String lower = sms.toLowerCase();

        if (lower.contains("debited")
                || lower.contains("withdrawn")
                || lower.contains("spent")
                || lower.contains("paid")) {

            return "DEBIT";
        }

        if (lower.contains("credited")
                || lower.contains("received")
                || lower.contains("deposited")) {

            return "CREDIT";
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

        if (upper.contains("ATM")) {
            return "ATM";
        }

        if (upper.contains("MOB BK")) {
            return "MOBILE_BANKING";
        }

        return "BANK";
    }

    // =====================================================
    // EXTRACT MERCHANT
    // =====================================================

    private String extractMerchant(String sms) {

        String upper = sms.toUpperCase();

        // Mobile banking
        if (upper.contains("MOB BK")) {
            return "Mobile Banking Transfer";
        }

        // ATM
        if (upper.contains("ATM")) {

            String atm =
                    extractMatch(
                            ATM_PATTERN,
                            sms
                    );

            if (atm != null) {
                return cleanMerchant(atm);
            }

            return "ATM Withdrawal";
        }

        // UPI
        if (upper.contains("UPI")) {

            String upi =
                    extractMatch(
                            UPI_PATTERN,
                            sms
                    );

            if (upi != null) {
                return cleanMerchant(upi);
            }
        }

        // VPA
        String vpa =
                extractMatch(
                        VPA_PATTERN,
                        sms
                );

        if (vpa != null) {
            return parseUPIMerchant(vpa);
        }

        // To
        String toMerchant =
                extractMatch(
                        TO_PATTERN,
                        sms
                );

        if (toMerchant != null) {
            return cleanMerchant(toMerchant);
        }

        // From
        String fromMerchant =
                extractMatch(
                        FROM_PATTERN,
                        sms
                );

        if (fromMerchant != null) {
            return cleanMerchant(fromMerchant);
        }

        return "Union Bank Transaction";
    }

    // =====================================================
    // PARSE UPI MERCHANT
    // =====================================================

    private String parseUPIMerchant(String vpa) {

        String clean = vpa.toLowerCase();

        if (clean.contains("paytm")) {
            return "Paytm";
        }

        if (clean.contains("phonepe")) {
            return "PhonePe";
        }

        if (clean.contains("gpay")
                || clean.contains("googlepay")) {

            return "Google Pay";
        }

        if (clean.contains("amazon")) {
            return "Amazon";
        }

        if (clean.contains("flipkart")) {
            return "Flipkart";
        }

        if (clean.contains("swiggy")) {
            return "Swiggy";
        }

        if (clean.contains("zomato")) {
            return "Zomato";
        }

        if (clean.contains("uber")) {
            return "Uber";
        }

        if (clean.contains("ola")) {
            return "Ola";
        }

        if (clean.matches("\\d+")) {
            return "Individual";
        }

        return cleanMerchant(clean);
    }
}