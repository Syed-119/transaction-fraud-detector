package com.fraud.service;

import com.fraud.dto.PredictionResult;
import com.fraud.dto.TransactionRequest;
import com.fraud.dto.TransactionResponse;
import com.fraud.mapper.TransactionMapper;
import com.fraud.model.Transaction;
import com.fraud.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private MlServiceClient mlServiceClient;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction buildTransaction(UUID id, BigDecimal amount, String merchant,
                                          String category, String card, boolean fraud, double confidence) {
        var t = new Transaction();
        t.setId(id);
        t.setAmount(amount);
        t.setMerchantName(merchant);
        t.setCategory(category);
        t.setCardLast4(card);
        t.setTransactionTime(LocalDateTime.now());
        t.setFraudulent(fraud);
        t.setConfidenceScore(confidence);
        return t;
    }

    private TransactionResponse buildResponse(UUID id, boolean fraud, double confidence) {
        return new TransactionResponse(
                id, new BigDecimal("250.00"), "Shop", "online", "1234",
                LocalDateTime.now(), fraud, confidence, LocalDateTime.now()
        );
    }

    @Test
    void analyseTransaction_fraudulent_setsFlagTrue() {
        var request = new TransactionRequest(
                new BigDecimal("9999.99"), "Suspicious Shop", "online", "1234", LocalDateTime.now()
        );
        var id = UUID.randomUUID();
        var saved = buildTransaction(id, request.amount(), request.merchantName(),
                request.category(), request.cardLast4(), true, 0.97);
        var expectedResponse = buildResponse(id, true, 0.97);

        when(mlServiceClient.predict(any())).thenReturn(new PredictionResult(true, 0.97));
        when(transactionRepository.save(any())).thenReturn(saved);
        when(transactionMapper.toResponse(saved)).thenReturn(expectedResponse);

        var response = transactionService.analyseTransaction(request);

        assertTrue(response.fraudulent());
        assertEquals(0.97, response.confidenceScore());
        verify(transactionRepository).save(any());
        verify(mlServiceClient).predict(request.amount());
    }

    @Test
    void analyseTransaction_legitimate_setsFlagFalse() {
        var request = new TransactionRequest(
                new BigDecimal("25.00"), "Tesco", "grocery", "5678", null
        );
        var id = UUID.randomUUID();
        var saved = buildTransaction(id, request.amount(), request.merchantName(),
                request.category(), request.cardLast4(), false, 0.92);
        var expectedResponse = buildResponse(id, false, 0.92);

        when(mlServiceClient.predict(any())).thenReturn(new PredictionResult(false, 0.92));
        when(transactionRepository.save(any())).thenReturn(saved);
        when(transactionMapper.toResponse(saved)).thenReturn(expectedResponse);

        var response = transactionService.analyseTransaction(request);

        assertFalse(response.fraudulent());
        verify(mlServiceClient).predict(request.amount());
    }

    @Test
    void analyseTransaction_noTimestamp_usesCurrentTime() {
        var request = new TransactionRequest(
                new BigDecimal("50.00"), "Shop", "retail", "9999", null
        );
        var id = UUID.randomUUID();
        var saved = buildTransaction(id, request.amount(), request.merchantName(),
                request.category(), request.cardLast4(), false, 0.85);
        var expectedResponse = buildResponse(id, false, 0.85);

        when(mlServiceClient.predict(any())).thenReturn(new PredictionResult(false, 0.85));
        when(transactionRepository.save(any())).thenReturn(saved);
        when(transactionMapper.toResponse(saved)).thenReturn(expectedResponse);

        transactionService.analyseTransaction(request);

        verify(transactionRepository).save(argThat(t -> t.getTransactionTime() != null));
    }

    @Test
    void getFlaggedTransactions_returnsFraudulentOnly() {
        var id = UUID.randomUUID();
        var fraudulent = buildTransaction(id, new BigDecimal("5000.00"), "Scam Inc",
                "online", "1111", true, 0.99);
        var expectedResponse = buildResponse(id, true, 0.99);

        when(transactionRepository.findByFraudulentTrue()).thenReturn(List.of(fraudulent));
        when(transactionMapper.toResponse(fraudulent)).thenReturn(expectedResponse);

        var result = transactionService.getFlaggedTransactions();

        assertEquals(1, result.size());
        assertTrue(result.get(0).fraudulent());
    }

    @Test
    void getFlaggedTransactions_noFraud_returnsEmpty() {
        when(transactionRepository.findByFraudulentTrue()).thenReturn(List.of());

        var result = transactionService.getFlaggedTransactions();

        assertTrue(result.isEmpty());
    }

    @Test
    void getTransactionsByCard_returnsMatchingTransactions() {
        var id = UUID.randomUUID();
        var transaction = buildTransaction(id, new BigDecimal("100.00"), "Store",
                "retail", "4444", false, 0.88);
        var expectedResponse = new TransactionResponse(
                id, new BigDecimal("100.00"), "Store", "retail", "4444",
                LocalDateTime.now(), false, 0.88, LocalDateTime.now()
        );

        when(transactionRepository.findByCardLast4("4444")).thenReturn(List.of(transaction));
        when(transactionMapper.toResponse(transaction)).thenReturn(expectedResponse);

        var result = transactionService.getTransactionsByCard("4444");

        assertEquals(1, result.size());
        assertEquals("4444", result.get(0).cardLast4());
    }

    @Test
    void getTransactionsByCard_noMatch_returnsEmpty() {
        when(transactionRepository.findByCardLast4("0000")).thenReturn(List.of());

        var result = transactionService.getTransactionsByCard("0000");

        assertTrue(result.isEmpty());
    }

    @Test
    void getTransaction_exists_returnsTransaction() {
        var id = UUID.randomUUID();
        var transaction = buildTransaction(id, new BigDecimal("75.00"), "Boots",
                "pharmacy", "3333", false, 0.91);
        var expectedResponse = new TransactionResponse(
                id, new BigDecimal("75.00"), "Boots", "pharmacy", "3333",
                LocalDateTime.now(), false, 0.91, LocalDateTime.now()
        );

        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toResponse(transaction)).thenReturn(expectedResponse);

        var result = transactionService.getTransaction(id);

        assertEquals(id, result.id());
        assertEquals("Boots", result.merchantName());
    }

    @Test
    void getTransaction_notFound_throwsException() {
        var id = UUID.randomUUID();
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> transactionService.getTransaction(id));
    }
}
