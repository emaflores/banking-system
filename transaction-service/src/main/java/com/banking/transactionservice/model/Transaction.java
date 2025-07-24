package com.banking.transactionservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originAccount;

    private String destinationAccount;

    private BigDecimal amount;

    private String currency;

    private LocalDateTime timestamp;

    private String performedBy; // Email del usuario autenticado
}
