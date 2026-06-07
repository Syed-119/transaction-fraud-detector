package com.fraud.service;

import com.fraud.dto.TransactionRequest;
import com.fraud.dto.TransactionResponse;
import com.fraud.model.Transaction;
import com.fraud.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final MlServiceClient mlServiceClient;

    public TransactionResponse analyseTransaction(TransactionRequest request){
        final var prediction = mlServiceClient.predict(request.getAmount());
        final var transaction = new Transaction();

        transaction.setAmount(request.getAmount());
        transaction.setMerchantName(request.getMerchantName());
        transaction.setCategory(request.getCategory());
        transaction.setCardLast4(request.getCardLast4());
        transaction.setTransactionTime(
                request.getTransactionTime() != null ? request.getTransactionTime() : LocalDateTime.now()
        );
        transaction.setFraudulent(prediction.fraud());
        transaction.setConfidenceScore(prediction.confidence());

        final var saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    public List<TransactionResponse> getFlaggedTransactions() {
        return transactionRepository.findByFraudulentTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<TransactionResponse> getTransactionsByCard(String cardLast4) {
        return transactionRepository.findByCardLast4(cardLast4).stream()
                .map(this::toResponse)
                .toList();
    }

    public TransactionResponse getTransaction(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
        return toResponse(transaction);
    }

    private TransactionResponse toResponse(Transaction t) {
        TransactionResponse response = new TransactionResponse();
        response.setId(t.getId());
        response.setAmount(t.getAmount());
        response.setMerchantName(t.getMerchantName());
        response.setCategory(t.getCategory());
        response.setCardLast4(t.getCardLast4());
        response.setTransactionTime(t.getTransactionTime());
        response.setFraudulent(t.getFraudulent());
        response.setConfidenceScore(t.getConfidenceScore());
        response.setCreatedAt(t.getCreatedAt());
        return response;
    }


}
