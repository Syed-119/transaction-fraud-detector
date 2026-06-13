package com.fraud.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        BigDecimal amount,
        String merchantName,
        String category,
        String cardLast4,
        LocalDateTime transactionTime,
        Boolean fraudulent,
        Double confidenceScore,
        LocalDateTime createdAt
) {}
