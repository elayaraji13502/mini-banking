package com.banfico.minibanking.consent.entity;

import com.banfico.minibanking.customer.entity.Customer;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "consents")
public class Consent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String purpose;

    @Column(
            name = "requested_data",
            nullable = false,
            length = 500
    )
    private String requestedData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConsentStatus status;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false
    )
    private Customer customer;

    public Consent() {
    }

    public Consent(
            String purpose,
            String requestedData,
            ConsentStatus status,
            Customer customer
    ) {
        this.purpose = purpose;
        this.requestedData = requestedData;
        this.status = status;
        this.customer = customer;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getRequestedData() {
        return requestedData;
    }

    public void setRequestedData(String requestedData) {
        this.requestedData = requestedData;
    }

    public ConsentStatus getStatus() {
        return status;
    }

    public void setStatus(ConsentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public Customer getCustomer() {
        return customer;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

