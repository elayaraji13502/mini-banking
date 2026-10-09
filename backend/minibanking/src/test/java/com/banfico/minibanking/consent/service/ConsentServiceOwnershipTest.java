package com.banfico.minibanking.consent.service;

import com.banfico.minibanking.consent.dto.ConsentRequest;
import com.banfico.minibanking.consent.repository.ConsentRepository;
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
class ConsentServiceOwnershipTest {

    @Mock
    private ConsentRepository consentRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    private ConsentService consentService;

    @BeforeEach
    void setUp() {
        consentService = new ConsentService(
                consentRepository,
                customerRepository,
                authenticatedUserService
        );
    }

    @Test
    void createConsentRejectsOtherCustomersResources() {
        ConsentRequest request = new ConsentRequest();
        request.setCustomerId(88L);
        request.setPurpose("Open banking access");
        request.setRequestedData("account balance and transactions");

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "customer1",
                        "pass",
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                );

        doThrow(new AccessDeniedException("You are not authorized to access this customer's resources"))
                .when(authenticatedUserService)
                .verifyCustomerOwnership(88L);

        assertThatThrownBy(() -> consentService.createConsent(request, authentication))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized");
    }
}
