package com.banfico.minibanking.consent.controller;

import com.banfico.minibanking.consent.dto.ConsentRequest;
import com.banfico.minibanking.consent.dto.ConsentResponse;
import com.banfico.minibanking.consent.service.ConsentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consents")
public class ConsentController {

    private final ConsentService consentService;

    public ConsentController(
            ConsentService consentService
    ) {
        this.consentService = consentService;
    }

    /*
     * CUSTOMER, MAKER and ADMIN can create consents.
     */
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MAKER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<ConsentResponse> createConsent(
            @Valid @RequestBody ConsentRequest request,
            Authentication authentication
    ) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(consentService.createConsent(
                        request,
                        authentication
                ));
    }

    /*
     * All authenticated banking roles can view consents.
     */
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MAKER', 'CHECKER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<ConsentResponse>> getAllConsents(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                consentService.getAllConsents(
                        authentication
                )
        );
    }

    /*
     * All authenticated banking roles can view a consent.
     */
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MAKER', 'CHECKER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ConsentResponse> getConsentById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                consentService.getConsentById(
                        id,
                        authentication
                )
        );
    }

    /*
     * All authenticated banking roles can query
     * consents by customer.
     *
     * Resource ownership will be added later.
     */
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MAKER', 'CHECKER', 'ADMIN')")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<ConsentResponse>> getConsentsByCustomer(
            @PathVariable Long customerId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                consentService.getConsentsByCustomer(
                        customerId,
                        authentication
                )
        );
    }

    /*
     * Only CHECKER and ADMIN can approve a consent.
     */
    @PreAuthorize("hasAnyRole('CHECKER', 'ADMIN')")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ConsentResponse> approveConsent(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                consentService.approveConsent(id)
        );
    }

    /*
     * Only CHECKER and ADMIN can reject a consent.
     */
    @PreAuthorize("hasAnyRole('CHECKER', 'ADMIN')")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ConsentResponse> rejectConsent(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                consentService.rejectConsent(id)
        );
    }
}