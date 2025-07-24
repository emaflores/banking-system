package com.banking.accountservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InternalTransferRequest {
    private String originAccount;
    private String destinationAccount;
    private BigDecimal amount;
}
