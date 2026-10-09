package com.banfico.minibanking.consent.service;

import com.banfico.minibanking.consent.dto.ConsentRequest;
import com.banfico.minibanking.consent.dto.ConsentResponse;
import com.banfico.minibanking.consent.entity.Consent;
import com.banfico.minibanking.consent.entity.ConsentStatus;
import com.banfico.minibanking.consent.repository.ConsentRepository;
import com.banfico.minibanking.customer.entity.Customer;
import com.banfico.minibanking.customer.repository.CustomerRepository;
import com.banfico.minibanking.exception.ResourceNotFoundException;
import com.banfico.minibanking.security.AuthenticatedUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsentService {

    private final ConsentRepository consentRepository;
    private final CustomerRepository customerRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public ConsentService(
            ConsentRepository consentRepository,
            CustomerRepository customerRepository,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.consentRepository = consentRepository;
        this.customerRepository = customerRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Transactional
    public ConsentResponse createConsent(
            ConsentRequest request,
            Authentication authentication
    ) {

        verifyCustomerOwnershipIfRequired(
                request.getCustomerId(),
                authentication
        );

        Customer customer = customerRepository
                .findById(request.getCustomerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: "
                                        + request.getCustomerId()
                        )
                );

        Consent consent = new Consent(
                request.getPurpose(),
                request.getRequestedData(),
                ConsentStatus.PENDING,
                customer
        );

        Consent savedConsent =
                consentRepository.save(consent);

        return ConsentResponse.fromEntity(
                savedConsent
        );
    }

    @Transactional(readOnly = true)
    public ConsentResponse getConsentById(
            Long id,
            Authentication authentication
    ) {

        Consent consent =
                consentRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Consent not found with id: "
                                                + id
                                )
                        );

        verifyCustomerOwnershipIfRequired(
                consent.getCustomer().getId(),
                authentication
        );

        return ConsentResponse.fromEntity(consent);
    }

    @Transactional(readOnly = true)
    public List<ConsentResponse> getAllConsents(
            Authentication authentication
    ) {

        if (isCustomer(authentication)) {
            Long customerId = authenticatedUserService
                    .getCurrentCustomerId();

            return consentRepository
                    .findByCustomerIdOrderByCreatedAtDesc(customerId)
                    .stream()
                    .map(ConsentResponse::fromEntity)
                    .toList();
        }

        return consentRepository.findAll()
                .stream()
                .map(ConsentResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConsentResponse> getConsentsByCustomer(
            Long customerId,
            Authentication authentication
    ) {

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer not found with id: "
                            + customerId
            );
        }

        verifyCustomerOwnershipIfRequired(
                customerId,
                authentication
        );

        return consentRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(ConsentResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ConsentResponse approveConsent(Long id) {

        Consent consent = getConsentEntity(id);

        validatePendingStatus(consent);

        consent.setStatus(ConsentStatus.APPROVED);

        // At this stage the entity needs a setter for approvedAt.
        consent.setApprovedAt(LocalDateTime.now());

        Consent updatedConsent =
                consentRepository.save(consent);

        return ConsentResponse.fromEntity(
                updatedConsent
        );
    }

    @Transactional
    public ConsentResponse rejectConsent(Long id) {

        Consent consent = getConsentEntity(id);

        validatePendingStatus(consent);

        consent.setStatus(ConsentStatus.REJECTED);

        // At this stage the entity needs a setter for rejectedAt.
        consent.setRejectedAt(LocalDateTime.now());

        Consent updatedConsent =
                consentRepository.save(consent);

        return ConsentResponse.fromEntity(
                updatedConsent
        );
    }

    private Consent getConsentEntity(Long id) {

        return consentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Consent not found with id: "
                                        + id
                        )
                );
    }

    private void validatePendingStatus(
            Consent consent
    ) {

        if (consent.getStatus()
                != ConsentStatus.PENDING) {

            throw new IllegalArgumentException(
                    "Only pending consents can be approved or rejected"
            );
        }
    }

    private void verifyCustomerOwnershipIfRequired(
            Long customerId,
            Authentication authentication
    ) {
        if (!isCustomer(authentication)) {
            return;
        }

        authenticatedUserService.verifyCustomerOwnership(
                customerId
        );
    }

    private boolean isCustomer(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_CUSTOMER")
                );
    }
}