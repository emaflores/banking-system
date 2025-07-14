package com.banking.transactionservice.service;

import com.banking.transactionservice.client.AccountClient;
import com.banking.transactionservice.dto.TransferRequest;
import com.banking.transactionservice.model.Transaction;
import com.banking.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository repository;
    private final AccountClient accountClient;
    private final KafkaProducerService kafkaProducer;

    public Transaction transfer(TransferRequest request, String userEmail, String jwt) {
        boolean ok = accountClient.validateAndProcessTransfer(
                request.getOriginAccount(),
                request.getDestinationAccount(),
                request.getAmount(),
                jwt
        );

        if (!ok) {
            throw new RuntimeException("No se pudo procesar la transferencia.");
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

        kafkaProducer.sendTransferEvent("Transferencia " + saved.getId() + " completada");

        return saved;
    }

    public List<Transaction> getAllByUser(String userEmail) {
        return repository.findAllByPerformedBy(userEmail);
    }
}
