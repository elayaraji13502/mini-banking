package com.banfico.minibanking.transaction.entity;

import com.banfico.minibanking.account.entity.BankAccount;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "transaction_reference",
            nullable = false,
            unique = true,
            length = 50
    )
    private String transactionReference;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private TransactionType type;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private TransactionStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "account_id",
            nullable = false
    )
    private BankAccount account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "destination_account_id"
    )
    private BankAccount destinationAccount;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    public Transaction() {
    }

    // Constructor for deposit and withdrawal
    public Transaction(
            String transactionReference,
            TransactionType type,
            BigDecimal amount,
            TransactionStatus status,
            BankAccount account
    ) {
        this.transactionReference = transactionReference;
        this.type = type;
        this.amount = amount;
        this.status = status;
        this.account = account;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for transfer
    public Transaction(
            String transactionReference,
            TransactionType type,
            BigDecimal amount,
            TransactionStatus status,
            BankAccount account,
            BankAccount destinationAccount
    ) {
        this.transactionReference = transactionReference;
        this.type = type;
        this.amount = amount;
        this.status = status;
        this.account = account;
        this.destinationAccount = destinationAccount;
        this.createdAt = LocalDateTime.now();
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

    public BankAccount getAccount() {
        return account;
    }

    public BankAccount getDestinationAccount() {
        return destinationAccount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}