package com.banking.transactionservice.service;

import com.banking.transactionservice.client.AccountClient;
import com.banking.transactionservice.dto.TransferRequest;
import com.banking.transactionservice.model.Transaction;
import com.banking.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository repository;
    private final AccountClient accountClient;
    private final KafkaProducerService kafkaProducer;

    public Transaction transfer(TransferRequest request, String userEmail, String jwt) {
        log.info("Processing transfer request from user: {}", userEmail);
        
        try {
            CompletableFuture<Boolean> transferResult = accountClient.validateAndProcessTransfer(
                    request.getOriginAccount(),
                    request.getDestinationAccount(),
                    request.getAmount(),
                    jwt
            );

            boolean ok = transferResult.get(10, TimeUnit.SECONDS);
            
            if (!ok) {
                log.error("Transfer failed for user: {}", userEmail);
                throw new RuntimeException("No se pudo procesar la transferencia.");
            }
            
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Error processing transfer for user: {}, error: {}", userEmail, e.getMessage());
            throw new RuntimeException("Error en la comunicación con el servicio de cuentas: " + e.getMessage());
        }

        Transaction tx = Transaction.builder()
                .originAccount(request.getOriginAccount())
                .destinationAccount(request.getDestinationAccount())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .timestamp(LocalDateTime.now())
                .performedBy(userEmail)
                .build();

        Transaction saved = repository.save(tx);
        log.info("Transaction saved with ID: {}", saved.getId());

        kafkaProducer.sendTransferEvent("Transferencia " + saved.getId() + " completada");
        log.info("Transfer event sent to Kafka for transaction: {}", saved.getId());

        return saved;
    }

    public List<Transaction> getAllByUser(String userEmail) {
        return repository.findAllByPerformedBy(userEmail);
    }
}
