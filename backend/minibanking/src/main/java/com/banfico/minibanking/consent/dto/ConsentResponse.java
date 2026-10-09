package com.banfico.minibanking.consent.dto;

import com.banfico.minibanking.consent.entity.Consent;
import com.banfico.minibanking.consent.entity.ConsentStatus;

import java.time.LocalDateTime;

public class ConsentResponse {

    private Long id;
    private Long customerId;
    private String purpose;
    private String requestedData;
    private ConsentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;

    public ConsentResponse() {
    }

    public ConsentResponse(
            Long id,
            Long customerId,
            String purpose,
            String requestedData,
            ConsentStatus status,
            LocalDateTime createdAt,
            LocalDateTime approvedAt,
            LocalDateTime rejectedAt
    ) {
        this.id = id;
        this.customerId = customerId;
        this.purpose = purpose;
        this.requestedData = requestedData;
        this.status = status;
        this.createdAt = createdAt;
        this.approvedAt = approvedAt;
        this.rejectedAt = rejectedAt;
    }

    public static ConsentResponse fromEntity(
            Consent consent
    ) {

        return new ConsentResponse(
                consent.getId(),
                consent.getCustomer().getId(),
                consent.getPurpose(),
                consent.getRequestedData(),
                consent.getStatus(),
                consent.getCreatedAt(),
                consent.getApprovedAt(),
                consent.getRejectedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getRequestedData() {
        return requestedData;
    }

    public ConsentStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }
}