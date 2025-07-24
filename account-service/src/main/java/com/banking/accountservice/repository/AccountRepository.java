package com.banking.accountservice.repository;

import com.banking.accountservice.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByOwnerEmail(String ownerEmail);
    Optional<Account> findByAccountNumber(String accountNumber);

}
