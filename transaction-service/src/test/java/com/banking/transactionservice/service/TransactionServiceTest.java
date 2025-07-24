package com.banking.transactionservice.service;

import com.banking.transactionservice.client.AccountClient;
import com.banking.transactionservice.dto.TransferRequest;
import com.banking.transactionservice.exception.TransferException;
import com.banking.transactionservice.model.Transaction;
import com.banking.transactionservice.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private AccountClient accountClient;

    @Mock
    private KafkaProducerService kafkaProducer;

    @InjectMocks
    private TransactionService transactionService;

    private TransferRequest transferRequest;
    private Transaction mockTransaction;

    @BeforeEach
    void setUp() {
        transferRequest = new TransferRequest();
        transferRequest.setOriginAccount("550e8400-e29b-41d4-a716-446655440000");
        transferRequest.setDestinationAccount("550e8400-e29b-41d4-a716-446655440001");
        transferRequest.setAmount(BigDecimal.valueOf(100.00));
        transferRequest.setCurrency("USD");

        mockTransaction = Transaction.builder()
                .id(1L)
                .originAccount(transferRequest.getOriginAccount())
                .destinationAccount(transferRequest.getDestinationAccount())
                .amount(transferRequest.getAmount())
                .currency(transferRequest.getCurrency())
                .timestamp(LocalDateTime.now())
                .performedBy("test@example.com")
                .build();
    }

    @Test
    void should_create_transaction_when_transfer_successful() {
        // Given
        String userEmail = "test@example.com";
        String jwt = "valid-jwt-token";
        
        when(accountClient.validateAndProcessTransfer(any(), any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(true));
        when(repository.save(any(Transaction.class))).thenReturn(mockTransaction);

        // When
        Transaction result = transactionService.transfer(transferRequest, userEmail, jwt);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOriginAccount()).isEqualTo(transferRequest.getOriginAccount());
        assertThat(result.getDestinationAccount()).isEqualTo(transferRequest.getDestinationAccount());
        assertThat(result.getAmount()).isEqualTo(transferRequest.getAmount());
        assertThat(result.getPerformedBy()).isEqualTo(userEmail);
        
        verify(accountClient).validateAndProcessTransfer(
                transferRequest.getOriginAccount(),
                transferRequest.getDestinationAccount(),
                transferRequest.getAmount(),
                jwt
        );
        verify(repository).save(any(Transaction.class));
        verify(kafkaProducer).sendTransferEvent(contains("completada"));
    }

    @Test
    void should_throw_exception_when_account_service_fails() {
        // Given
        String userEmail = "test@example.com";
        String jwt = "valid-jwt-token";
        
        when(accountClient.validateAndProcessTransfer(any(), any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(false));

        // When / Then
        assertThatThrownBy(() -> transactionService.transfer(transferRequest, userEmail, jwt))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se pudo procesar la transferencia");

        verify(accountClient).validateAndProcessTransfer(any(), any(), any(), any());
        verify(repository, never()).save(any(Transaction.class));
        verify(kafkaProducer, never()).sendTransferEvent(any());
    }

    @Test
    void should_throw_exception_when_account_service_timeout() {
        // Given
        String userEmail = "test@example.com";
        String jwt = "valid-jwt-token";
        
        CompletableFuture<Boolean> timeoutFuture = new CompletableFuture<>();
        // Don't complete the future to simulate timeout
        
        when(accountClient.validateAndProcessTransfer(any(), any(), any(), any()))
                .thenReturn(timeoutFuture);

        // When / Then
        assertThatThrownBy(() -> transactionService.transfer(transferRequest, userEmail, jwt))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error en la comunicación con el servicio de cuentas");

        verify(accountClient).validateAndProcessTransfer(any(), any(), any(), any());
        verify(repository, never()).save(any(Transaction.class));
        verify(kafkaProducer, never()).sendTransferEvent(any());
    }

    @Test
    void should_return_all_transactions_for_user() {
        // Given
        String userEmail = "test@example.com";
        List<Transaction> mockTransactions = List.of(
                mockTransaction,
                Transaction.builder()
                        .id(2L)
                        .originAccount("550e8400-e29b-41d4-a716-446655440002")
                        .destinationAccount("550e8400-e29b-41d4-a716-446655440003")
                        .amount(BigDecimal.valueOf(200.00))
                        .currency("USD")
                        .timestamp(LocalDateTime.now())
                        .performedBy(userEmail)
                        .build()
        );
        
        when(repository.findAllByPerformedBy(userEmail)).thenReturn(mockTransactions);

        // When
        List<Transaction> result = transactionService.getAllByUser(userEmail);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPerformedBy()).isEqualTo(userEmail);
        assertThat(result.get(1).getPerformedBy()).isEqualTo(userEmail);
        
        verify(repository).findAllByPerformedBy(userEmail);
    }

    @Test
    void should_return_empty_list_when_no_transactions_found() {
        // Given
        String userEmail = "test@example.com";
        
        when(repository.findAllByPerformedBy(userEmail)).thenReturn(List.of());

        // When
        List<Transaction> result = transactionService.getAllByUser(userEmail);

        // Then
        assertThat(result).isEmpty();
        
        verify(repository).findAllByPerformedBy(userEmail);
    }
}