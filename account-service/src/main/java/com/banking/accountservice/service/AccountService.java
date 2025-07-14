package com.banking.accountservice.service;

import com.banking.accountservice.dto.InternalTransferRequest;
import com.banking.accountservice.model.Account;
import com.banking.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Account createAccount(String email) {
        Account account = Account.builder()
                .ownerEmail(email)
                .balance(BigDecimal.ZERO)
                .accountNumber(UUID.randomUUID().toString())
                .currency("ARS")
                .build();

        return accountRepository.save(account);
    }

    public List<Account> getAccounts(String email) {
        return accountRepository.findByOwnerEmail(email);
    }

    public Account getById(Long id, String email) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        if (!account.getOwnerEmail().equals(email)) {
            throw new RuntimeException("No autorizado");
        }

        return account;
    }

    public void transfer(InternalTransferRequest request, String email) {
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
    }

}
