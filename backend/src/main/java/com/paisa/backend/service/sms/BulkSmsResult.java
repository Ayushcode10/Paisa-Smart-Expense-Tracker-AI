package com.paisa.backend.service.sms;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkSmsResult {
    private int totalReceived;
    private int successCount;
    private int failureCount;
    private List<String> failedSmsPreview;  // so you can debug which ones failed
}