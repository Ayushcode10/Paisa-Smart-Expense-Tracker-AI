package com.paisa.backend.service.sms.parser.sbi;

import com.paisa.backend.service.sms.ParsedSmsResult;
import com.paisa.backend.service.sms.parser.BaseBankParser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class SbiParser extends BaseBankParser {

    private static final Pattern DEBIT_PATTERN =
            Pattern.compile(
                    "debited by\\s+(\\d+\\.?\\d*)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern CREDIT_PATTERN =
            Pattern.compile(
                    "credited by\\s+Rs\\.?\\s*(\\d+\\.?\\d*)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern ACCOUNT_PATTERN =
            Pattern.compile(
                    "A/c\\s+([X*\\d]+)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern BALANCE_PATTERN =
            Pattern.compile(
                    "Avl\\s+Bal\\s+Rs\\.?\\s*(\\d+\\.?\\d*)",
                    Pattern.CASE_INSENSITIVE
            );

    @Override
    public boolean matches(String sms) {

        sms = normalizeSms(sms);
        String upper = sms.toUpperCase();

        return upper.contains("SBI")
                || upper.contains("SBIINB")
                || upper.contains("SBIBK");
    }

    @Override
    public ParsedSmsResult parse(String sms) {

        sms = normalizeSms(sms);
        BigDecimal amount =
                extractBigDecimal(DEBIT_PATTERN, sms);

        String type = "DEBIT";

        if (amount == null) {

            amount =
                    extractBigDecimal(CREDIT_PATTERN, sms);

            type = "CREDIT";
        }

        if (amount == null) {
            return ParsedSmsResult.failed(
                    "SBI parse failed"
            );
        }

        String account =
                extractMatch(ACCOUNT_PATTERN, sms);

        BigDecimal balance =
                extractBigDecimal(BALANCE_PATTERN, sms);

        return ParsedSmsResult.builder()
                .success(true)
                .amount(amount)
                .type(type)
                .merchant("SBI Transaction")
                .accountLast4(account)
                .availableBalance(balance)
                .source("BANK")
                .bankName("SBI")
                .transactionDate(LocalDateTime.now())
                .rawSms(sms)
                .build();
    }
}