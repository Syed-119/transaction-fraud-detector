package com.fraud.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="transactions")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "merchant_name", nullable = false)
    private String merchantName;

    @Column(nullable = false)
    private String category;

    @Column(name = "card_last4", nullable = false, length = 4)
    private String cardLast4;

    @Column(name = "transaction_time", nullable = false)
    private LocalDateTime transactionTime;

    @Column(nullable = false)
    private Boolean fraudulent;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
