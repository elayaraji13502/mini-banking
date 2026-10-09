package com.banfico.minibanking.transaction.dto;

import com.banfico.minibanking.transaction.entity.Transaction;
import com.banfico.minibanking.transaction.entity.TransactionStatus;
import com.banfico.minibanking.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private Long id;
    private String transactionReference;
    private TransactionType type;
    private BigDecimal amount;
    private TransactionStatus status;
    private Long accountId;
    private Long destinationAccountId;
    private LocalDateTime createdAt;

    public TransactionResponse() {
    }

    public TransactionResponse(
            Long id,
            String transactionReference,
            TransactionType type,
            BigDecimal amount,
            TransactionStatus status,
            Long accountId,
            Long destinationAccountId,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.transactionReference = transactionReference;
        this.type = type;
        this.amount = amount;
        this.status = status;
        this.accountId = accountId;
        this.destinationAccountId = destinationAccountId;
        this.createdAt = createdAt;
    }

    public static TransactionResponse fromEntity(
            Transaction transaction
    ) {

        Long destinationAccountId = null;

        if (transaction.getDestinationAccount() != null) {
            destinationAccountId =
                    transaction.getDestinationAccount().getId();
        }

        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionReference(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getAccount().getId(),
                destinationAccountId,
                transaction.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public Long getAccountId() {
        return accountId;
    }

    public Long getDestinationAccountId() {
        return destinationAccountId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}