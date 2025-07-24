package com.banking.userservice.exception;

import com.banking.userservice.dto.ErrorResponse;
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

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .code("USER_ALREADY_EXISTS")
                .message("El usuario ya existe en el sistema")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        
        log.warn("User already exists: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .code("INVALID_CREDENTIALS")
                .message("Credenciales inválidas")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        
        log.warn("Invalid credentials: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
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