package com.fraud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud.dto.TransactionRequest;
import com.fraud.dto.TransactionResponse;
import com.fraud.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private TransactionResponse buildResponse(boolean fraudulent, double confidence) {
        return new TransactionResponse(
                UUID.randomUUID(),
                new BigDecimal("250.00"),
                "Amazon",
                "online",
                "1234",
                LocalDateTime.now(),
                fraudulent,
                confidence,
                LocalDateTime.now()
        );
    }

    @Test
    void analyseTransaction_returnsCreatedWithResponse() throws Exception {
        var request = new TransactionRequest(
                new BigDecimal("250.00"), "Amazon", "online", "1234", null
        );
        var response = buildResponse(false, 0.95);

        when(transactionService.analyseTransaction(any())).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fraudulent").value(false))
                .andExpect(jsonPath("$.confidenceScore").value(0.95));
    }

    @Test
    void analyseTransaction_missingRequiredFields_returnsBadRequest() throws Exception {
        var request = new TransactionRequest(null, null, null, null, null);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void analyseTransaction_negativeAmount_returnsBadRequest() throws Exception {
        var request = new TransactionRequest(
                new BigDecimal("-10.00"), "Shop", "retail", "5678", null
        );

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void analyseTransaction_invalidCardLast4_returnsBadRequest() throws Exception {
        var request = new TransactionRequest(
                new BigDecimal("50.00"), "Shop", "retail", "12", null
        );

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getFlaggedTransactions_returnsList() throws Exception {
        var flagged = buildResponse(true, 0.98);

        when(transactionService.getFlaggedTransactions()).thenReturn(List.of(flagged));

        mockMvc.perform(get("/api/transactions/flagged"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fraudulent").value(true))
                .andExpect(jsonPath("$[0].confidenceScore").value(0.98));
    }

    @Test
    void getFlaggedTransactions_empty_returnsEmptyList() throws Exception {
        when(transactionService.getFlaggedTransactions()).thenReturn(List.of());

        mockMvc.perform(get("/api/transactions/flagged"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getByCard_returnsList() throws Exception {
        var response = buildResponse(false, 0.90);

        when(transactionService.getTransactionsByCard("1234")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/transactions/card/1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cardLast4").value("1234"));
    }

    @Test
    void getTransaction_returnsTransaction() throws Exception {
        var id = UUID.randomUUID();
        var response = new TransactionResponse(
                id, new BigDecimal("100.00"), "Tesco", "grocery", "5678",
                LocalDateTime.now(), false, 0.92, LocalDateTime.now()
        );

        when(transactionService.getTransaction(id)).thenReturn(response);

        mockMvc.perform(get("/api/transactions/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.merchantName").value("Tesco"));
    }
}
