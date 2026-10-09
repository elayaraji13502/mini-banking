package com.banfico.minibanking.transaction.repository;

import com.banfico.minibanking.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionReference(
            String transactionReference
    );

    /*
     * Returns transactions where the account is either:
     *
     * 1. The source/account of the transaction
     * OR
     * 2. The destination account of a transfer
     *
     * This is important because a customer should see
     * incoming transfers in their transaction history.
     */
    List<Transaction>
    findByAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(
            Long accountId,
            Long destinationAccountId
    );
}