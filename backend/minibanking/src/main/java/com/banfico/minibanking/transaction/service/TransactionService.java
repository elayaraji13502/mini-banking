package com.banfico.minibanking.transaction.service;

import com.banfico.minibanking.account.entity.AccountStatus;
import com.banfico.minibanking.account.entity.BankAccount;
import com.banfico.minibanking.account.repository.BankAccountRepository;
import com.banfico.minibanking.exception.ResourceNotFoundException;
import com.banfico.minibanking.security.AuthenticatedUserService;
import com.banfico.minibanking.transaction.dto.TransactionRequest;
import com.banfico.minibanking.transaction.dto.TransactionResponse;
import com.banfico.minibanking.transaction.dto.TransferRequest;
import com.banfico.minibanking.transaction.entity.Transaction;
import com.banfico.minibanking.transaction.entity.TransactionStatus;
import com.banfico.minibanking.transaction.entity.TransactionType;
import com.banfico.minibanking.transaction.repository.TransactionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public TransactionService(
            TransactionRepository transactionRepository,
            BankAccountRepository bankAccountRepository,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.transactionRepository = transactionRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    // ============================================================
    // DEPOSIT
    // ============================================================

    @Transactional
    public TransactionResponse deposit(
            TransactionRequest request
    ) {

        // 1. Find account
        BankAccount account =
                getAccount(request.getAccountId());

        // 2. Account must be active
        validateAccountIsActive(account);

        // 3. Increase balance
        BigDecimal newBalance =
                account.getBalance()
                        .add(request.getAmount());

        account.setBalance(newBalance);

        // 4. Save updated account
        bankAccountRepository.save(account);

        // 5. Create transaction
        Transaction transaction = new Transaction(
                generateTransactionReference(),
                TransactionType.DEPOSIT,
                request.getAmount(),
                TransactionStatus.SUCCESS,
                account
        );

        // 6. Save transaction
        Transaction savedTransaction =
                transactionRepository.save(transaction);

        // 7. Return response
        return TransactionResponse.fromEntity(
                savedTransaction
        );
    }


    // ============================================================
    // WITHDRAWAL
    // ============================================================

    @Transactional
    public TransactionResponse withdraw(
            TransactionRequest request
    ) {

        // 1. Find account
        BankAccount account =
                getAccount(request.getAccountId());

        // 2. Account must be active
        validateAccountIsActive(account);

        // 3. Check sufficient balance
        if (account.getBalance()
                .compareTo(request.getAmount()) < 0) {

            throw new IllegalArgumentException(
                    "Insufficient account balance"
            );
        }

        // 4. Decrease balance
        BigDecimal newBalance =
                account.getBalance()
                        .subtract(request.getAmount());

        account.setBalance(newBalance);

        // 5. Save account
        bankAccountRepository.save(account);

        // 6. Create transaction
        Transaction transaction = new Transaction(
                generateTransactionReference(),
                TransactionType.WITHDRAWAL,
                request.getAmount(),
                TransactionStatus.SUCCESS,
                account
        );

        // 7. Save transaction
        Transaction savedTransaction =
                transactionRepository.save(transaction);

        // 8. Return response
        return TransactionResponse.fromEntity(
                savedTransaction
        );
    }


    // ============================================================
    // TRANSFER
    // ============================================================

    @Transactional
    public TransactionResponse transfer(
            TransferRequest request
    ) {

        // 1. Source and destination must be different
        if (request.getSourceAccountId()
                .equals(request.getDestinationAccountId())) {

            throw new IllegalArgumentException(
                    "Source and destination accounts must be different"
            );
        }

        // 2. Find source account
        BankAccount sourceAccount =
                getAccount(request.getSourceAccountId());

        // 3. Find destination account
        BankAccount destinationAccount =
                getAccount(request.getDestinationAccountId());

        // 4. Both accounts must be active
        validateAccountIsActive(sourceAccount);
        validateAccountIsActive(destinationAccount);

        // 5. Check sufficient balance
        if (sourceAccount.getBalance()
                .compareTo(request.getAmount()) < 0) {

            throw new IllegalArgumentException(
                    "Insufficient balance in source account"
            );
        }

        // 6. Deduct from source
        sourceAccount.setBalance(
                sourceAccount.getBalance()
                        .subtract(request.getAmount())
        );

        // 7. Add to destination
        destinationAccount.setBalance(
                destinationAccount.getBalance()
                        .add(request.getAmount())
        );

        // 8. Save source
        bankAccountRepository.save(sourceAccount);

        // 9. Save destination
        bankAccountRepository.save(destinationAccount);

        // 10. Create transfer transaction
        Transaction transaction = new Transaction(
                generateTransactionReference(),
                TransactionType.TRANSFER,
                request.getAmount(),
                TransactionStatus.SUCCESS,
                sourceAccount,
                destinationAccount
        );

        // 11. Save transaction
        Transaction savedTransaction =
                transactionRepository.save(transaction);

        // 12. Return response
        return TransactionResponse.fromEntity(
                savedTransaction
        );
    }


    // ============================================================
    // GET TRANSACTIONS BY ACCOUNT
    // ============================================================

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByAccount(
            Long accountId,
            Authentication authentication
    ) {

        // 1. Find account
        BankAccount account = getAccount(accountId);

        // 2. CUSTOMER can access only their own account
        verifyCustomerOwnershipIfRequired(
                account,
                authentication
        );

        /*
         * Get transactions where:
         *
         * account_id = current account
         *
         * OR
         *
         * destination_account_id = current account
         *
         * This includes both outgoing and incoming transfers.
         */
        return transactionRepository
                .findByAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(
                        accountId,
                        accountId
                )
                .stream()
                .map(TransactionResponse::fromEntity)
                .toList();
    }


    // ============================================================
    // GET TRANSACTION BY ID
    // ============================================================

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(
            Long id,
            Authentication authentication
    ) {

        Transaction transaction =
                transactionRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Transaction not found with id: "
                                                + id
                                )
                        );

        /*
         * For a normal transaction:
         *
         * account → customer
         *
         * For a transfer:
         *
         * source account → customer
         * destination account → customer
         *
         * CUSTOMER is allowed if either account belongs
         * to the authenticated customer.
         */
        verifyTransactionOwnershipIfRequired(
                transaction,
                authentication
        );

        return TransactionResponse.fromEntity(
                transaction
        );
    }


    // ============================================================
    // CUSTOMER OWNERSHIP - ACCOUNT
    // ============================================================

    private void verifyCustomerOwnershipIfRequired(
            BankAccount account,
            Authentication authentication
    ) {

        boolean isCustomer =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_CUSTOMER")
                        );

        /*
         * MAKER, CHECKER and ADMIN are not restricted
         * by customer ownership.
         */
        if (!isCustomer) {
            return;
        }

        authenticatedUserService.verifyCustomerOwnership(
                account.getCustomer().getId()
        );
    }


    // ============================================================
    // CUSTOMER OWNERSHIP - TRANSACTION
    // ============================================================

    private void verifyTransactionOwnershipIfRequired(
            Transaction transaction,
            Authentication authentication
    ) {

        boolean isCustomer =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_CUSTOMER")
                        );

        /*
         * MAKER, CHECKER and ADMIN can view transactions
         * without customer ownership restriction.
         */
        if (!isCustomer) {
            return;
        }

        /*
         * Normal deposit/withdrawal transaction.
         */
        BankAccount account =
                transaction.getAccount();

        if (account != null
                && authenticatedUserService.isCurrentCustomer(
                        account.getCustomer().getId()
        )) {
            return;
        }

        /*
         * Transfer transaction.
         *
         * Customer is allowed if they own either:
         *
         * source account
         * OR
         * destination account
         */
        BankAccount destinationAccount =
                transaction.getDestinationAccount();

        if (destinationAccount != null
                && authenticatedUserService.isCurrentCustomer(
                        destinationAccount.getCustomer().getId()
        )) {
            return;
        }

        throw new org.springframework.security.access.AccessDeniedException(
                "You are not authorized to access this transaction"
        );
    }


    // ============================================================
    // FIND ACCOUNT
    // ============================================================

    private BankAccount getAccount(
            Long accountId
    ) {

        return bankAccountRepository
                .findById(accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bank account not found with id: "
                                        + accountId
                        )
                );
    }


    // ============================================================
    // VALIDATE ACCOUNT STATUS
    // ============================================================

    private void validateAccountIsActive(
            BankAccount account
    ) {

        if (account.getStatus()
                != AccountStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "Transactions are allowed only on active accounts"
            );
        }
    }


    // ============================================================
    // GENERATE TRANSACTION REFERENCE
    // ============================================================

    private String generateTransactionReference() {

        return "TXN-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase();
    }
}