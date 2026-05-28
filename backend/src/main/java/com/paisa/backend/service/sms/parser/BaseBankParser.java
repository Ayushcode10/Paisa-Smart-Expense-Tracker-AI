package com.paisa.backend.service.sms.parser;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseParser implements BankParser {

    protected String cleanMerchant(String raw) {

        if (raw == null || raw.isBlank()) {
            return "Unknown";
        }

        String cleaned = raw
                .replaceAll("@.*", "")
                .replaceAll("\\d+", "")
                .replaceAll("[*@#&]", "")
                .replaceAll("[_\\-/]", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();

        String[] words = cleaned.split(" ");

        StringBuilder result = new StringBuilder();

        for (String word : words) {

            if (!word.isEmpty()) {

                result.append(
                        Character.toUpperCase(word.charAt(0))
                );

                if (word.length() > 1) {
                    result.append(word.substring(1));
                }

                result.append(" ");
            }
        }

        return result.toString().trim();
    }

    protected BigDecimal extractBigDecimal(
            Pattern pattern,
            String sms
    ) {

        Matcher matcher = pattern.matcher(sms);

        if (matcher.find()) {

            try {

                String cleaned =
                        matcher.group(1)
                                .replace(",", "")
                                .trim();

                return new BigDecimal(cleaned);

            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    protected String extractMatch(
            Pattern pattern,
            String sms
    ) {

        Matcher matcher = pattern.matcher(sms);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }
}