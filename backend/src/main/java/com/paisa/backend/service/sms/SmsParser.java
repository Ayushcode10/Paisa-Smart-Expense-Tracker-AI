package com.paisa.backend.service.sms;

import com.paisa.backend.service.sms.parser.BankParser;
import com.paisa.backend.service.sms.parser.axis.AxisBankParser;
import com.paisa.backend.service.sms.parser.hdfc.HdfcParser;
import com.paisa.backend.service.sms.parser.hdfc.HdfcMf;
import com.paisa.backend.service.sms.parser.hsbc.HsbcParser;
import com.paisa.backend.service.sms.parser.icici.IciciParser;
import com.paisa.backend.service.sms.parser.sbi.SbiParser;
import com.paisa.backend.service.sms.parser.union.UnionBankParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SmsParser {

    // List of all bank parsers
    private final List<BankParser> parsers;

    // Constructor
    public SmsParser() {

        this.parsers = List.of(

                // HDFC parser
                new HdfcParser(),
                new HdfcMf(),
                new IciciParser(),
                new SbiParser(),
                new UnionBankParser(),
                new HsbcParser(),
                new AxisBankParser()
        );
    }

    // Main parse method
    public ParsedSmsResult parse(String smsText) {

        // Validate input
        if (smsText == null || smsText.isBlank()) {
            return ParsedSmsResult.failed("Empty SMS text");
        }

        String cleanedSms = smsText.trim();

        // Try every parser one by one
        for (BankParser parser : parsers) {

            try {

                // Check if parser supports this SMS
                if (parser.matches(cleanedSms)) {

                    log.debug(
                            "Trying parser: {}",
                            parser.getClass().getSimpleName()
                    );

                    ParsedSmsResult result = parser.parse(cleanedSms);

                    // Successful parse
                    if (result != null && result.isSuccess()) {

                        log.info(
                                "SMS parsed successfully using {}",
                                parser.getClass().getSimpleName()
                        );

                        return result;
                    }
                }

            } catch (Exception e) {

                // Don't crash entire parser system
                // One parser failure should not stop others

                log.warn(
                        "Parser {} failed: {}",
                        parser.getClass().getSimpleName(),
                        e.getMessage()
                );
            }
        }

        // No parser matched
        log.warn("No parser matched SMS: {}", cleanedSms);

        return ParsedSmsResult.failed(
                "No matching bank pattern found"
        );


    }
}