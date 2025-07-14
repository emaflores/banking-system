package com.banking.transactionservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    private String originAccount;
    private String destinationAccount;
    private BigDecimal amount;
    private String currency;
}
