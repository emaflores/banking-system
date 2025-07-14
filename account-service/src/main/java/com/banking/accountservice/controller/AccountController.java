package com.banking.accountservice.controller;

import com.banking.accountservice.dto.InternalTransferRequest;
import com.banking.accountservice.model.Account;
import com.banking.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<Account> create(Principal principal) {
        return ResponseEntity.ok(accountService.createAccount(principal.getName()));
    }

    @GetMapping
    public ResponseEntity<List<Account>> list(Principal principal) {
        return ResponseEntity.ok(accountService.getAccounts(principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> get(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(accountService.getById(id, principal.getName()));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@RequestBody InternalTransferRequest request,
                                         Principal principal) {
        accountService.transfer(request, principal.getName());
        return ResponseEntity.ok().build();
    }

}
