package com.paisa.backend.service.sms.parser.icici;

import com.paisa.backend.service.sms.ParsedSmsResult;
import com.paisa.backend.service.sms.parser.BaseBankParser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class IciciParser extends BaseBankParser {

    private static final Pattern DEBIT_PATTERN =
            Pattern.compile(
                    "debited.*Rs\\.?\\s*([\\d,]+\\.?\\d*)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern CREDIT_PATTERN =
            Pattern.compile(
                    "credited.*Rs\\.?\\s*([\\d,]+\\.?\\d*)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern ACCOUNT_PATTERN =
            Pattern.compile(
                    "Acct\\s+([X*\\d]+)",
                    Pattern.CASE_INSENSITIVE
            );

    @Override
    public boolean matches(String sms) {
        sms = normalizeSms(sms);
        String upper = sms.toUpperCase();

        return upper.contains("ICICI")
                || upper.contains("ICICIB");
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
                    "ICICI parse failed"
            );
        }

        String account =
                extractMatch(ACCOUNT_PATTERN, sms);

        return ParsedSmsResult.builder()
                .success(true)
                .amount(amount)
                .type(type)
                .merchant("ICICI Transaction")
                .accountLast4(account)
                .source("BANK")
                .bankName("ICICI")
                .transactionDate(LocalDateTime.now())
                .rawSms(sms)
                .build();
    }
}