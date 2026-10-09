package com.banfico.minibanking.beneficiary.service;

import com.banfico.minibanking.beneficiary.dto.BeneficiaryRequest;
import com.banfico.minibanking.beneficiary.repository.BeneficiaryRepository;
import com.banfico.minibanking.customer.repository.CustomerRepository;
import com.banfico.minibanking.security.AuthenticatedUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class BeneficiaryServiceOwnershipTest {

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    private BeneficiaryService beneficiaryService;

    @BeforeEach
    void setUp() {
        beneficiaryService = new BeneficiaryService(
                beneficiaryRepository,
                customerRepository,
                authenticatedUserService
        );
    }

    @Test
    void createBeneficiaryRejectsOtherCustomersResources() {
        BeneficiaryRequest request = new BeneficiaryRequest();
        request.setCustomerId(99L);
        request.setName("Test Beneficiary");
        request.setAccountNumber("1234567890");
        request.setBankName("Banfico");

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "customer1",
                        "pass",
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                );

        doThrow(new AccessDeniedException("You are not authorized to access this customer's resources"))
                .when(authenticatedUserService)
                .verifyCustomerOwnership(99L);

        assertThatThrownBy(() -> beneficiaryService.createBeneficiary(request, authentication))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized");
    }
}
