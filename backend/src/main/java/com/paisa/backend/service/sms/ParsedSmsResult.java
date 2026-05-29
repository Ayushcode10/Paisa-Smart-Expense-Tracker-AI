package com.paisa.backend.service.sms;


import lombok.*;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedSmsResult {
    private boolean success;      //did we successfully parse the sms?
    private String failureReason; //if not, why?

    private BigDecimal amount;
    private String type;           //Debit or Credit
    private String merchant;
    private String accountLast4;    //Last 4 digits of account
    private String source;          //upi, card, net_banking,atm
    private String bankName;        //hdfc, sbi, union,etc..
    private BigDecimal availableBalance;
    private LocalDateTime transactionDate;
    private String rawSms;          //og raw sms for debugging



    // convenience method
    public static ParsedSmsResult failed(String reason){
        return ParsedSmsResult.builder()
                .success(false)
                .failureReason(reason)
                .build();
    }

}
