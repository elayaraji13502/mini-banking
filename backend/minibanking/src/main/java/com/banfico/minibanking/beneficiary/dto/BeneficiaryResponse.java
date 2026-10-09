package com.banfico.minibanking.beneficiary.dto;

import com.banfico.minibanking.beneficiary.entity.Beneficiary;
import com.banfico.minibanking.beneficiary.entity.BeneficiaryStatus;

import java.time.LocalDateTime;

public class BeneficiaryResponse {

    private Long id;
    private String name;
    private String accountNumber;
    private String bankName;
    private BeneficiaryStatus status;
    private Long customerId;
    private LocalDateTime createdAt;

    public BeneficiaryResponse() {
    }

    public BeneficiaryResponse(
            Long id,
            String name,
            String accountNumber,
            String bankName,
            BeneficiaryStatus status,
            Long customerId,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.name = name;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.status = status;
        this.customerId = customerId;
        this.createdAt = createdAt;
    }

    public static BeneficiaryResponse fromEntity(
            Beneficiary beneficiary
    ) {

        return new BeneficiaryResponse(
                beneficiary.getId(),
                beneficiary.getName(),
                beneficiary.getAccountNumber(),
                beneficiary.getBankName(),
                beneficiary.getStatus(),
                beneficiary.getCustomer().getId(),
                beneficiary.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public BeneficiaryStatus getStatus() {
        return status;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}