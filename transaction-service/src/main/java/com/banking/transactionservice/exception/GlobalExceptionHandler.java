package com.banking.transactionservice.exception;

import com.banking.transactionservice.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        ErrorResponse error = ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message(errorMessage)
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        
        log.warn("Validation error: {}", errorMessage);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        String errorMessage = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        
        ErrorResponse error = ErrorResponse.builder()
                .code("CONSTRAINT_VIOLATION")
                .message(errorMessage)
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        
        log.warn("Constraint violation: {}", errorMessage);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .code("INSUFFICIENT_FUNDS")
                .message("Fondos insuficientes para realizar la transferencia")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        
        log.warn("Insufficient funds: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(TransferException.class)
    public ResponseEntity<ErrorResponse> handleTransferException(TransferException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .code("TRANSFER_ERROR")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        
        log.error("Transfer error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .code("INVALID_ARGUMENT")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        
        log.warn("Invalid argument: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message("Ha ocurrido un error interno del servidor")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}