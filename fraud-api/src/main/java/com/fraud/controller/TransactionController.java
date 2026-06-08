package com.fraud.controller;

import com.fraud.dto.TransactionRequest;
import com.fraud.dto.TransactionResponse;
import com.fraud.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> analyseTransaction(
            @Valid @RequestBody TransactionRequest transactionRequest){
        final var response = transactionService.analyseTransaction(transactionRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/flagged")
    public ResponseEntity<List<TransactionResponse>> getFlaggedTransaction(){
        return ResponseEntity.ok(transactionService.getFlaggedTransactions());
    }

    @GetMapping("/card/{cardLast4}")
    public ResponseEntity<List<TransactionResponse>> getByCard(
            @PathVariable String cardLast4) {
        return ResponseEntity.ok(transactionService.getTransactionsByCard(cardLast4));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable UUID id) {
        return ResponseEntity.ok(transactionService.getTransaction(id));
    }

}
