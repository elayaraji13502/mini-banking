package com.banfico.minibanking.account.dto;

import com.banfico.minibanking.account.entity.AccountStatus;
import com.banfico.minibanking.account.entity.AccountType;
import com.banfico.minibanking.account.entity.BankAccount;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BankAccountResponse {

    private Long id;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private AccountStatus status;
    private Long customerId;
    private LocalDateTime createdAt;

    public BankAccountResponse() {
    }

    public BankAccountResponse(
            Long id,
            String accountNumber,
            AccountType accountType,
            BigDecimal balance,
            AccountStatus status,
            Long customerId,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
        this.customerId = customerId;
        this.createdAt = createdAt;
    }

    public static BankAccountResponse fromEntity(
            BankAccount account
    ) {

        return new BankAccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getStatus(),
                account.getCustomer().getId(),
                account.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}