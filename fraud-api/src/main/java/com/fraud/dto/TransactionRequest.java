package com.fraud.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank String merchantName,
        @NotBlank String category,
        @NotBlank @Size(min = 4, max = 4) String cardLast4,
        LocalDateTime transactionTime
) {}
