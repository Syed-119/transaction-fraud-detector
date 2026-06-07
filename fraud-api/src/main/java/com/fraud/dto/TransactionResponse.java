package com.fraud.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionResponse {
    private UUID id;
    private BigDecimal amount;
    private String merchantName;
    private String category;
    private String cardLast4;
    private LocalDateTime transactionTime;
    private Boolean fraudulent;
    private Double confidenceScore;
    private LocalDateTime createdAt;
}
