package com.paisa.backend.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


//all fields are optional here, user might want to change them
@Data
public class UpdateTransactionRequest {
    private BigDecimal amount;
    private String merchant;
    private String category;
    private String type;
    private String source;
    private String note;
    private List<String> tags;
}
