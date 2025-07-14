package com.banking.transactionservice.controller;

import com.banking.transactionservice.dto.TransferRequest;
import com.banking.transactionservice.model.Transaction;
import com.banking.transactionservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody TransferRequest request,
                                              @RequestHeader("Authorization") String auth,
                                              Principal principal) {
        String token = auth.replace("Bearer ", "");
        return ResponseEntity.ok(service.transfer(request, principal.getName(), token));
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAll(Principal principal) {
        return ResponseEntity.ok(service.getAllByUser(principal.getName()));
    }
}
