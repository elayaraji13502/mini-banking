package com.banfico.minibanking.consent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ConsentRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Purpose is required")
    @Size(
            max = 200,
            message = "Purpose must not exceed 200 characters"
    )
    private String purpose;

    @NotBlank(message = "Requested data is required")
    @Size(
            max = 500,
            message = "Requested data must not exceed 500 characters"
    )
    private String requestedData;

    public ConsentRequest() {
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
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
}