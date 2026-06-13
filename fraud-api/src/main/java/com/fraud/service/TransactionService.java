package com.fraud.service;

import com.fraud.dto.TransactionRequest;
import com.fraud.dto.TransactionResponse;
import com.fraud.mapper.TransactionMapper;
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
    private final TransactionMapper transactionMapper;

    public TransactionResponse analyseTransaction(TransactionRequest request){
        final var prediction = mlServiceClient.predict(request.amount());
        final var transaction = new Transaction();

        transaction.setAmount(request.amount());
        transaction.setMerchantName(request.merchantName());
        transaction.setCategory(request.category());
        transaction.setCardLast4(request.cardLast4());
        transaction.setTransactionTime(
                request.transactionTime() != null ? request.transactionTime() : LocalDateTime.now()
        );
        transaction.setFraudulent(prediction.fraud());
        transaction.setConfidenceScore(prediction.confidence());

        final var saved = transactionRepository.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    public List<TransactionResponse> getFlaggedTransactions() {
        return transactionRepository.findByFraudulentTrue().stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    public List<TransactionResponse> getTransactionsByCard(String cardLast4) {
        return transactionRepository.findByCardLast4(cardLast4).stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    public TransactionResponse getTransaction(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
        return transactionMapper.toResponse(transaction);
    }


}
