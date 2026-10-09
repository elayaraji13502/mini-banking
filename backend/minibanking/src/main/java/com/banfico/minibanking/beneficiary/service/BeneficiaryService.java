package com.banfico.minibanking.beneficiary.service;

import com.banfico.minibanking.beneficiary.dto.BeneficiaryRequest;
import com.banfico.minibanking.beneficiary.dto.BeneficiaryResponse;
import com.banfico.minibanking.beneficiary.entity.Beneficiary;
import com.banfico.minibanking.beneficiary.entity.BeneficiaryStatus;
import com.banfico.minibanking.beneficiary.repository.BeneficiaryRepository;
import com.banfico.minibanking.customer.entity.Customer;
import com.banfico.minibanking.customer.repository.CustomerRepository;
import com.banfico.minibanking.exception.DuplicateResourceException;
import com.banfico.minibanking.exception.ResourceNotFoundException;
import com.banfico.minibanking.security.AuthenticatedUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final CustomerRepository customerRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public BeneficiaryService(
            BeneficiaryRepository beneficiaryRepository,
            CustomerRepository customerRepository,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.beneficiaryRepository = beneficiaryRepository;
        this.customerRepository = customerRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Transactional
    public BeneficiaryResponse createBeneficiary(
            BeneficiaryRequest request,
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

        boolean alreadyExists =
                beneficiaryRepository
                        .existsByCustomerIdAndAccountNumber(
                                request.getCustomerId(),
                                request.getAccountNumber()
                        );

        if (alreadyExists) {
            throw new DuplicateResourceException(
                    "This account is already added as a beneficiary"
            );
        }

        Beneficiary beneficiary = new Beneficiary(
                request.getName(),
                request.getAccountNumber(),
                request.getBankName(),
                BeneficiaryStatus.ACTIVE,
                customer
        );

        Beneficiary savedBeneficiary =
                beneficiaryRepository.save(beneficiary);

        return BeneficiaryResponse.fromEntity(
                savedBeneficiary
        );
    }

    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getAllBeneficiaries(
            Authentication authentication
    ) {

        if (isCustomer(authentication)) {
            Long customerId = authenticatedUserService
                    .getCurrentCustomerId();

            return beneficiaryRepository.findByCustomerId(customerId)
                    .stream()
                    .map(BeneficiaryResponse::fromEntity)
                    .toList();
        }

        return beneficiaryRepository.findAll()
                .stream()
                .map(BeneficiaryResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public BeneficiaryResponse getBeneficiaryById(
            Long id,
            Authentication authentication
    ) {

        Beneficiary beneficiary =
                beneficiaryRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Beneficiary not found with id: "
                                                + id
                                )
                        );

        verifyCustomerOwnershipIfRequired(
                beneficiary.getCustomer().getId(),
                authentication
        );

        return BeneficiaryResponse.fromEntity(
                beneficiary
        );
    }

    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getBeneficiariesByCustomer(
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

        return beneficiaryRepository
                .findByCustomerId(customerId)
                .stream()
                .map(BeneficiaryResponse::fromEntity)
                .toList();
    }

    @Transactional
    public BeneficiaryResponse blockBeneficiary(
            Long id
    ) {

        Beneficiary beneficiary =
                beneficiaryRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Beneficiary not found with id: "
                                                + id
                                )
                        );

        if (beneficiary.getStatus()
                == BeneficiaryStatus.BLOCKED) {

            throw new IllegalArgumentException(
                    "Beneficiary is already blocked"
            );
        }

        beneficiary.setStatus(
                BeneficiaryStatus.BLOCKED
        );

        Beneficiary updatedBeneficiary =
                beneficiaryRepository.save(beneficiary);

        return BeneficiaryResponse.fromEntity(
                updatedBeneficiary
        );
    }

    @Transactional
    public BeneficiaryResponse activateBeneficiary(
            Long id
    ) {

        Beneficiary beneficiary =
                beneficiaryRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Beneficiary not found with id: "
                                                + id
                                )
                        );

        if (beneficiary.getStatus()
                == BeneficiaryStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "Beneficiary is already active"
            );
        }

        beneficiary.setStatus(
                BeneficiaryStatus.ACTIVE
        );

        Beneficiary updatedBeneficiary =
                beneficiaryRepository.save(beneficiary);

        return BeneficiaryResponse.fromEntity(
                updatedBeneficiary
        );
    }

    @Transactional
    public void deleteBeneficiary(Long id) {

        if (!beneficiaryRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Beneficiary not found with id: "
                            + id
            );
        }

        beneficiaryRepository.deleteById(id);
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