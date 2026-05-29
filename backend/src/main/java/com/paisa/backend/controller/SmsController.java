package com.paisa.backend.controller;

import com.paisa.backend.service.sms.BulkSmsResult;
import com.paisa.backend.service.sms.ParsedSmsResult;
import com.paisa.backend.service.sms.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    // Single SMS — real time as new SMS arrives on phone
    @PostMapping("/parse")
    public ResponseEntity<ParsedSmsResult> parseSingle(
            @RequestBody Map<String, String> body) {
        String smsText = body.get("sms");
        return ResponseEntity.ok(smsService.parseAndSave(smsText));
    }

    // Bulk SMS — on first launch, import last 6 months of history
    @PostMapping("/parse/bulk")
    public ResponseEntity<BulkSmsResult> parseBulk(
            @RequestBody Map<String, List<String>> body) {
        List<String> smsList = body.get("messages");
        return ResponseEntity.ok(smsService.parseAndSaveBulk(smsList));
    }

    // Preview only — parse but DON'T save (for testing / confirmation screen)
    @PostMapping("/parse/preview")
    public ResponseEntity<ParsedSmsResult> previewParse(
            @RequestBody Map<String, String> body) {
        String smsText = body.get("sms");
        return ResponseEntity.ok(smsService.previewParse(smsText));
    }
}