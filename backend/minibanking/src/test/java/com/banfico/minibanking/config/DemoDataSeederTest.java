package com.banfico.minibanking.config;

import com.banfico.minibanking.customer.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DemoDataSeederTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void demoCustomersAreSeededForKeycloakUsers() {
        assertThat(customerRepository.findByKeycloakUsername("customer")).isPresent();
        assertThat(customerRepository.findByKeycloakUsername("maker")).isPresent();
        assertThat(customerRepository.findByKeycloakUsername("checker")).isPresent();
        assertThat(customerRepository.findByKeycloakUsername("admin")).isPresent();
    }
}
