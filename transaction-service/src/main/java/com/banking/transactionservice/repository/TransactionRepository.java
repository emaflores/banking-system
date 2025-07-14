package com.banking.transactionservice.repository;

import com.banking.transactionservice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByPerformedBy(String userEmail);
}
