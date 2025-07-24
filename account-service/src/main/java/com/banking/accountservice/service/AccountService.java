package com.banking.accountservice.service;

import com.banking.accountservice.dto.InternalTransferRequest;
import com.banking.accountservice.model.Account;
import com.banking.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    @CacheEvict(value = "accounts", key = "#email")
    public Account createAccount(String email) {
        log.info("Creating new account for user: {}", email);
        
        Account account = Account.builder()
                .ownerEmail(email)
                .balance(BigDecimal.ZERO)
                .accountNumber(UUID.randomUUID().toString())
                .currency("ARS")
                .build();

        Account saved = accountRepository.save(account);
        log.info("Account created with ID: {} for user: {}", saved.getId(), email);
        
        return saved;
    }

    @Cacheable(value = "accounts", key = "#email")
    public List<Account> getAccounts(String email) {
        log.info("Fetching accounts for user: {}", email);
        List<Account> accounts = accountRepository.findByOwnerEmail(email);
        log.info("Found {} accounts for user: {}", accounts.size(), email);
        return accounts;
    }

    @Cacheable(value = "balances", key = "#id + '_' + #email")
    public Account getById(Long id, String email) {
        log.info("Fetching account {} for user: {}", id, email);
        
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        if (!account.getOwnerEmail().equals(email)) {
            throw new RuntimeException("No autorizado");
        }

        return account;
    }

    @Transactional
    @CacheEvict(value = {"accounts", "balances"}, allEntries = true)
    public void transfer(InternalTransferRequest request, String email) {
        log.info("Processing transfer from {} to {} for amount: {}", 
                 request.getOriginAccount(), request.getDestinationAccount(), request.getAmount());
                 
        Account origin = accountRepository.findByAccountNumber(request.getOriginAccount())
                .orElseThrow(() -> new RuntimeException("Cuenta origen no encontrada"));

        Account destination = accountRepository.findByAccountNumber(request.getDestinationAccount())
                .orElseThrow(() -> new RuntimeException("Cuenta destino no encontrada"));

        if (!origin.getOwnerEmail().equals(email)) {
            throw new RuntimeException("No autorizado a transferir desde esta cuenta");
        }

        if (origin.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Saldo insuficiente");
        }

        origin.setBalance(origin.getBalance().subtract(request.getAmount()));
        destination.setBalance(destination.getBalance().add(request.getAmount()));

        accountRepository.save(origin);
        accountRepository.save(destination);
        
        log.info("Transfer completed successfully between {} and {}", 
                 request.getOriginAccount(), request.getDestinationAccount());
    }

}
