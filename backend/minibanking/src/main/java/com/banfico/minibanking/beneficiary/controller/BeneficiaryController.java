package com.banfico.minibanking.beneficiary.controller;

import com.banfico.minibanking.beneficiary.dto.BeneficiaryRequest;
import com.banfico.minibanking.beneficiary.dto.BeneficiaryResponse;
import com.banfico.minibanking.beneficiary.service.BeneficiaryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(
            BeneficiaryService beneficiaryService
    ) {
        this.beneficiaryService = beneficiaryService;
    }

    /*
     * CUSTOMER, MAKER and ADMIN can create beneficiaries.
     */
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MAKER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<BeneficiaryResponse> createBeneficiary(
            @Valid @RequestBody BeneficiaryRequest request,
            Authentication authentication
    ) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(beneficiaryService.createBeneficiary(
                        request,
                        authentication
                ));
    }

    /*
     * All authenticated banking roles can view beneficiaries.
     */
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MAKER', 'CHECKER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<BeneficiaryResponse>> getAllBeneficiaries(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                beneficiaryService.getAllBeneficiaries(
                        authentication
                )
        );
    }

    /*
     * All authenticated banking roles can view a beneficiary.
     */
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MAKER', 'CHECKER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<BeneficiaryResponse> getBeneficiaryById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                beneficiaryService.getBeneficiaryById(
                        id,
                        authentication
                )
        );
    }

    /*
     * All authenticated roles can query beneficiaries by customer.
     *
     * IMPORTANT:
     * Resource ownership will be enforced in a later step.
     */
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MAKER', 'CHECKER', 'ADMIN')")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BeneficiaryResponse>> getByCustomer(
            @PathVariable Long customerId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                beneficiaryService.getBeneficiariesByCustomer(
                        customerId,
                        authentication
                )
        );
    }

    /*
     * Only CHECKER and ADMIN can block beneficiaries.
     */
    @PreAuthorize("hasAnyRole('CHECKER', 'ADMIN')")
    @PatchMapping("/{id}/block")
    public ResponseEntity<BeneficiaryResponse> blockBeneficiary(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                beneficiaryService.blockBeneficiary(id)
        );
    }

    /*
     * Only CHECKER and ADMIN can activate beneficiaries.
     */
    @PreAuthorize("hasAnyRole('CHECKER', 'ADMIN')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<BeneficiaryResponse> activateBeneficiary(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                beneficiaryService.activateBeneficiary(id)
        );
    }

    /*
     * Only CHECKER and ADMIN can delete beneficiaries.
     */
    @PreAuthorize("hasAnyRole('CHECKER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBeneficiary(
            @PathVariable Long id
    ) {

        beneficiaryService.deleteBeneficiary(id);

        return ResponseEntity.noContent().build();
    }
}