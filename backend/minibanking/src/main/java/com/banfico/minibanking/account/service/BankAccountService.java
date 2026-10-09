package com.banfico.minibanking.account.service;

import com.banfico.minibanking.account.dto.BankAccountRequest;
import com.banfico.minibanking.account.dto.BankAccountResponse;
import com.banfico.minibanking.account.entity.AccountStatus;
import com.banfico.minibanking.account.entity.BankAccount;
import com.banfico.minibanking.account.repository.BankAccountRepository;
import com.banfico.minibanking.customer.entity.Customer;
import com.banfico.minibanking.customer.repository.CustomerRepository;
import com.banfico.minibanking.exception.DuplicateResourceException;
import com.banfico.minibanking.exception.ResourceNotFoundException;
import com.banfico.minibanking.security.AuthenticatedUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final CustomerRepository customerRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public BankAccountService(
            BankAccountRepository bankAccountRepository,
            CustomerRepository customerRepository,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.bankAccountRepository = bankAccountRepository;
        this.customerRepository = customerRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Transactional
    public BankAccountResponse createAccount(
            BankAccountRequest request
    ) {

        // Only ADMIN reaches this method because of @PreAuthorize
        // in the controller.

        Customer customer = customerRepository
                .findById(request.getCustomerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: "
                                        + request.getCustomerId()
                        )
                );

        String accountNumber = generateUniqueAccountNumber();

        BigDecimal initialDeposit =
                request.getInitialDeposit();

        BankAccount account = new BankAccount(
                accountNumber,
                request.getAccountType(),
                initialDeposit,
                AccountStatus.ACTIVE,
                customer
        );

        BankAccount savedAccount =
                bankAccountRepository.save(account);

        return BankAccountResponse.fromEntity(savedAccount);
    }

    /**
     * Returns all accounts for staff/admin users.
     *
     * For CUSTOMER, only accounts belonging to the
     * authenticated customer are returned.
     */
    @Transactional(readOnly = true)
    public List<BankAccountResponse> getAllAccounts(
            Authentication authentication
    ) {

        boolean isCustomer = authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_CUSTOMER")
                );

        if (isCustomer) {

            Long customerId =
                    authenticatedUserService
                            .getCurrentCustomerId();

            return bankAccountRepository
                    .findByCustomerId(customerId)
                    .stream()
                    .map(BankAccountResponse::fromEntity)
                    .toList();
        }

        return bankAccountRepository.findAll()
                .stream()
                .map(BankAccountResponse::fromEntity)
                .toList();
    }

    /**
     * Returns an account by ID.
     *
     * CUSTOMER can only access their own account.
     *
     * MAKER, CHECKER and ADMIN can access the account.
     */
    @Transactional(readOnly = true)
    public BankAccountResponse getAccountById(
            Long id,
            Authentication authentication
    ) {

        BankAccount account = bankAccountRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bank account not found with id: "
                                        + id
                        )
                );

        verifyCustomerOwnershipIfRequired(
                account,
                authentication
        );

        return BankAccountResponse.fromEntity(account);
    }

    /**
     * Returns accounts belonging to a particular customer.
     *
     * CUSTOMER can only request their own customer ID.
     *
     * Staff/admin users can request any customer ID.
     */
    @Transactional(readOnly = true)
    public List<BankAccountResponse> getAccountsByCustomer(
            Long customerId,
            Authentication authentication
    ) {

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer not found with id: " + customerId
            );
        }

        verifyCustomerOwnershipIfRequired(
                customerId,
                authentication
        );

        return bankAccountRepository
                .findByCustomerId(customerId)
                .stream()
                .map(BankAccountResponse::fromEntity)
                .toList();
    }

    @Transactional
    public BankAccountResponse blockAccount(Long id) {

        BankAccount account = bankAccountRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bank account not found with id: "
                                        + id
                        )
                );

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "Closed account cannot be blocked"
            );
        }

        account.setStatus(AccountStatus.BLOCKED);

        BankAccount updatedAccount =
                bankAccountRepository.save(account);

        return BankAccountResponse.fromEntity(updatedAccount);
    }

    @Transactional
    public BankAccountResponse activateAccount(Long id) {

        BankAccount account = bankAccountRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bank account not found with id: "
                                        + id
                        )
                );

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "Closed account cannot be activated"
            );
        }

        account.setStatus(AccountStatus.ACTIVE);

        BankAccount updatedAccount =
                bankAccountRepository.save(account);

        return BankAccountResponse.fromEntity(updatedAccount);
    }

    @Transactional
    public BankAccountResponse closeAccount(Long id) {

        BankAccount account = bankAccountRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bank account not found with id: "
                                        + id
                        )
                );

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "Account is already closed"
            );
        }

        if (account.getBalance()
                .compareTo(BigDecimal.ZERO) != 0) {

            throw new IllegalArgumentException(
                    "Account balance must be zero before closing"
            );
        }

        account.setStatus(AccountStatus.CLOSED);

        BankAccount updatedAccount =
                bankAccountRepository.save(account);

        return BankAccountResponse.fromEntity(updatedAccount);
    }

    /**
     * Checks ownership when the authenticated user is CUSTOMER.
     *
     * Staff roles are not restricted by customer ownership here.
     */
    private void verifyCustomerOwnershipIfRequired(
            BankAccount account,
            Authentication authentication
    ) {

        boolean isCustomer = authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_CUSTOMER")
                );

        if (!isCustomer) {
            return;
        }

        authenticatedUserService.verifyCustomerOwnership(
                account.getCustomer().getId()
        );
    }

    /**
     * Checks ownership for a requested customer ID when
     * the authenticated user is CUSTOMER.
     */
    private void verifyCustomerOwnershipIfRequired(
            Long customerId,
            Authentication authentication
    ) {

        boolean isCustomer = authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_CUSTOMER")
                );

        if (!isCustomer) {
            return;
        }

        authenticatedUserService.verifyCustomerOwnership(
                customerId
        );
    }

    private String generateUniqueAccountNumber() {

        String accountNumber;

        do {
            accountNumber = String.valueOf(
                    ThreadLocalRandom.current()
                            .nextLong(
                                    100_000_000_000L,
                                    1_000_000_000_000L
                            )
            );

        } while (
                bankAccountRepository
                        .existsByAccountNumber(accountNumber)
        );

        return accountNumber;
    }
}
