package com.fraud.repository;

import com.fraud.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();

        var legit = new Transaction();
        legit.setAmount(new BigDecimal("50.00"));
        legit.setMerchantName("Tesco");
        legit.setCategory("grocery");
        legit.setCardLast4("1234");
        legit.setTransactionTime(LocalDateTime.now());
        legit.setFraudulent(false);
        legit.setConfidenceScore(0.90);
        transactionRepository.save(legit);

        var fraud = new Transaction();
        fraud.setAmount(new BigDecimal("8000.00"));
        fraud.setMerchantName("Unknown Merchant");
        fraud.setCategory("online");
        fraud.setCardLast4("5678");
        fraud.setTransactionTime(LocalDateTime.now());
        fraud.setFraudulent(true);
        fraud.setConfidenceScore(0.97);
        transactionRepository.save(fraud);

        var secondForCard = new Transaction();
        secondForCard.setAmount(new BigDecimal("30.00"));
        secondForCard.setMerchantName("Boots");
        secondForCard.setCategory("pharmacy");
        secondForCard.setCardLast4("1234");
        secondForCard.setTransactionTime(LocalDateTime.now());
        secondForCard.setFraudulent(false);
        secondForCard.setConfidenceScore(0.85);
        transactionRepository.save(secondForCard);
    }

    @Test
    void findByFraudulentTrue_returnsOnlyFraudulent() {
        var result = transactionRepository.findByFraudulentTrue();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getFraudulent());
        assertEquals("5678", result.get(0).getCardLast4());
    }

    @Test
    void findByCardLast4_returnsMatchingTransactions() {
        var result = transactionRepository.findByCardLast4("1234");

        assertEquals(2, result.size());
        result.forEach(t -> assertEquals("1234", t.getCardLast4()));
    }

    @Test
    void findByCardLast4_noMatch_returnsEmpty() {
        var result = transactionRepository.findByCardLast4("0000");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByCategory_returnsMatching() {
        var result = transactionRepository.findByCategory("grocery");

        assertEquals(1, result.size());
        assertEquals("Tesco", result.get(0).getMerchantName());
    }

    @Test
    void findByCategory_noMatch_returnsEmpty() {
        var result = transactionRepository.findByCategory("travel");

        assertTrue(result.isEmpty());
    }

    @Test
    void save_generatesIdAndCreatedAt() {
        var t = new Transaction();
        t.setAmount(new BigDecimal("10.00"));
        t.setMerchantName("Test");
        t.setCategory("test");
        t.setCardLast4("9999");
        t.setTransactionTime(LocalDateTime.now());
        t.setFraudulent(false);
        t.setConfidenceScore(0.5);

        var saved = transactionRepository.save(t);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void findAll_returnsAllTransactions() {
        var result = transactionRepository.findAll();

        assertEquals(3, result.size());
    }
}
