package com.paisa.backend.service.sms.parser;

import com.paisa.backend.service.sms.ParsedSmsResult;

public interface BankParser {

    boolean matches(String sms);

    ParsedSmsResult parse(String sms);
}