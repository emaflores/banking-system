package com.banking.transactionservice.controller;

import com.banking.transactionservice.dto.TransferRequest;
import com.banking.transactionservice.model.Transaction;
import com.banking.transactionservice.service.TransactionService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Timed(name = "transaction.controller", description = "Transaction controller metrics")
public class TransactionController {

    private final TransactionService service;
    private final Counter transferCounter;
    private final Counter transferSuccessCounter;
    private final Counter transferFailureCounter;
    private final Timer transferProcessingTimer;

    @PostMapping
    @Timed(name = "bank.transfer.create", description = "Transfer creation time")
    public ResponseEntity<Transaction> create(@Valid @RequestBody TransferRequest request,
                                              @RequestHeader("Authorization") String auth,
                                              Principal principal) {
        transferCounter.increment();
        
        Timer.Sample sample = Timer.start();
        
        try {
            String token = auth.replace("Bearer ", "");
            Transaction result = service.transfer(request, principal.getName(), token);
            transferSuccessCounter.increment();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            transferFailureCounter.increment();
            throw e;
        } finally {
            sample.stop(transferProcessingTimer);
        }
    }

    @GetMapping
    @Timed(name = "bank.transactions.list", description = "Transaction list retrieval time")
    public ResponseEntity<List<Transaction>> getAll(Principal principal) {
        return ResponseEntity.ok(service.getAllByUser(principal.getName()));
    }
}
