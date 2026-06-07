package com.fraud.repository;

import com.fraud.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByFraudulentTrue();

    List<Transaction> findByCardLast4(String cardLast4);

    List<Transaction> findByCategory(String category);
}
