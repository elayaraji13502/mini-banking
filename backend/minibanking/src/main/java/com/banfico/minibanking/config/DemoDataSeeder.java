package com.banfico.minibanking.config;

import com.banfico.minibanking.account.entity.AccountStatus;
import com.banfico.minibanking.account.entity.AccountType;
import com.banfico.minibanking.account.entity.BankAccount;
import com.banfico.minibanking.account.repository.BankAccountRepository;
import com.banfico.minibanking.customer.entity.Customer;
import com.banfico.minibanking.customer.repository.CustomerRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DemoDataSeeder implements ApplicationRunner {

    private final CustomerRepository customerRepository;
    private final BankAccountRepository bankAccountRepository;

    public DemoDataSeeder(
            CustomerRepository customerRepository,
            BankAccountRepository bankAccountRepository
    ) {
        this.customerRepository = customerRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedCustomers();
        seedAccounts();
    }

    private void seedCustomers() {
        List<SeedCustomer> demoCustomers = List.of(
                new SeedCustomer(
                        "customer",
                        "Customer Demo User",
                        "customer@banfico.local",
                        "+15550000001"
                ),
                new SeedCustomer(
                        "maker",
                        "Maker Demo User",
                        "maker@banfico.local",
                        "+15550000002"
                ),
                new SeedCustomer(
                        "checker",
                        "Checker Demo User",
                        "checker@banfico.local",
                        "+15550000003"
                ),
                new SeedCustomer(
                        "admin",
                        "Admin Demo User",
                        "admin@banfico.local",
                        "+15550000004"
                )
        );

        for (SeedCustomer seed : demoCustomers) {
            if (!customerRepository.existsByKeycloakUsername(seed.username())) {
                customerRepository.save(
                        new Customer(
                                seed.name(),
                                seed.email(),
                                seed.phone(),
                                seed.username()
                        )
                );
            }
        }
    }

    private void seedAccounts() {
        if (bankAccountRepository.count() > 0) {
            return;
        }

        seedAccountFor("customer", "1000000001", new BigDecimal("2500.00"));
        seedAccountFor("maker", "1000000002", new BigDecimal("12000.00"));
        seedAccountFor("checker", "1000000003", new BigDecimal("7800.00"));
        seedAccountFor("admin", "1000000004", new BigDecimal("43000.00"));
    }

    private void seedAccountFor(
            String keycloakUsername,
            String accountNumber,
            BigDecimal balance
    ) {
        Customer customer = customerRepository
                .findByKeycloakUsername(keycloakUsername)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Required demo customer not found: "
                                        + keycloakUsername
                        )
                );

        bankAccountRepository.save(
                new BankAccount(
                        accountNumber,
                        AccountType.CURRENT,
                        balance,
                        AccountStatus.ACTIVE,
                        customer
                )
        );
    }

    private record SeedCustomer(
            String username,
            String name,
            String email,
            String phone
    ) {
    }
}
