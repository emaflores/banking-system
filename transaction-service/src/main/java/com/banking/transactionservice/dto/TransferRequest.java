package com.banking.transactionservice.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    
    @NotBlank(message = "Origin account is required")
    @Pattern(regexp = "^[0-9a-fA-F-]{36}$", message = "Invalid origin account format")
    private String originAccount;
    
    @NotBlank(message = "Destination account is required")
    @Pattern(regexp = "^[0-9a-fA-F-]{36}$", message = "Invalid destination account format")
    private String destinationAccount;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "1000000.00", message = "Amount exceeds maximum limit")
    @Digits(integer = 7, fraction = 2, message = "Amount format is invalid")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter code")
    private String currency;
}
