package com.banking.transactionservice.controller;

import com.banking.transactionservice.dto.TransferRequest;
import com.banking.transactionservice.exception.GlobalExceptionHandler;
import com.banking.transactionservice.model.Transaction;
import com.banking.transactionservice.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@ContextConfiguration(classes = {TransactionController.class, GlobalExceptionHandler.class})
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private Counter transferCounter;

    @MockBean
    private Counter transferSuccessCounter;

    @MockBean
    private Counter transferFailureCounter;

    @MockBean
    private Timer transferProcessingTimer;

    @Autowired
    private ObjectMapper objectMapper;

    private TransferRequest validTransferRequest;
    private Transaction mockTransaction;

    @BeforeEach
    void setUp() {
        validTransferRequest = new TransferRequest();
        validTransferRequest.setOriginAccount("550e8400-e29b-41d4-a716-446655440000");
        validTransferRequest.setDestinationAccount("550e8400-e29b-41d4-a716-446655440001");
        validTransferRequest.setAmount(BigDecimal.valueOf(100.00));
        validTransferRequest.setCurrency("USD");

        mockTransaction = Transaction.builder()
                .id(1L)
                .originAccount(validTransferRequest.getOriginAccount())
                .destinationAccount(validTransferRequest.getDestinationAccount())
                .amount(validTransferRequest.getAmount())
                .currency(validTransferRequest.getCurrency())
                .timestamp(LocalDateTime.now())
                .performedBy("test@example.com")
                .build();

        // Mock meter behavior
        when(transferProcessingTimer.recordCallable(any())).thenReturn(mockTransaction);
        Timer.Sample mockSample = mock(Timer.Sample.class);
        when(Timer.start()).thenReturn(mockSample);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void should_create_transaction_successfully() throws Exception {
        // Given
        when(transactionService.transfer(any(TransferRequest.class), eq("test@example.com"), anyString()))
                .thenReturn(mockTransaction);

        // When / Then
        mockMvc.perform(post("/transactions")
                        .with(user("test@example.com"))
                        .header("Authorization", "Bearer valid-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.originAccount").value(validTransferRequest.getOriginAccount()))
                .andExpect(jsonPath("$.destinationAccount").value(validTransferRequest.getDestinationAccount()))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.performedBy").value("test@example.com"));

        verify(transactionService).transfer(any(TransferRequest.class), eq("test@example.com"), eq("valid-jwt-token"));
        verify(transferCounter).increment();
        verify(transferSuccessCounter).increment();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void should_return_validation_error_for_invalid_request() throws Exception {
        // Given
        TransferRequest invalidRequest = new TransferRequest();
        invalidRequest.setOriginAccount("invalid-uuid");
        invalidRequest.setDestinationAccount("");
        invalidRequest.setAmount(BigDecimal.valueOf(-10));
        invalidRequest.setCurrency("INVALID");

        // When / Then
        mockMvc.perform(post("/transactions")
                        .with(user("test@example.com"))
                        .header("Authorization", "Bearer valid-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpected(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpected(jsonPath("$.message").exists());

        verify(transactionService, never()).transfer(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void should_handle_service_exception() throws Exception {
        // Given
        when(transactionService.transfer(any(TransferRequest.class), eq("test@example.com"), anyString()))
                .thenThrow(new RuntimeException("Transfer failed"));

        // When / Then
        mockMvc.perform(post("/transactions")
                        .with(user("test@example.com"))
                        .header("Authorization", "Bearer valid-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransferRequest)))
                .andExpect(status().isInternalServerError())
                .andExpected(jsonPath("$.code").value("INTERNAL_ERROR"));

        verify(transferCounter).increment();
        verify(transferFailureCounter).increment();
        verify(transferSuccessCounter, never()).increment();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void should_return_user_transactions() throws Exception {
        // Given
        List<Transaction> mockTransactions = List.of(mockTransaction);
        when(transactionService.getAllByUser("test@example.com")).thenReturn(mockTransactions);

        // When / Then
        mockMvc.perform(get("/transactions")
                        .with(user("test@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpected(jsonPath("$[0].id").value(1))
                .andExpected(jsonPath("$[0].performedBy").value("test@example.com"));

        verify(transactionService).getAllByUser("test@example.com");
    }

    @Test
    void should_return_unauthorized_without_authentication() throws Exception {
        // When / Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransferRequest)))
                .andExpect(status().isUnauthorized());

        verify(transactionService, never()).transfer(any(), any(), any());
    }
}