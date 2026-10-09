package com.banfico.minibanking.transaction.controller;

import com.banfico.minibanking.transaction.dto.TransactionRequest;
import com.banfico.minibanking.transaction.dto.TransactionResponse;
import com.banfico.minibanking.transaction.dto.TransferRequest;
import com.banfico.minibanking.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(
            TransactionService transactionService
    ) {
        this.transactionService = transactionService;
    }

    // ============================================================
    // DEPOSIT
    // ============================================================

    @PreAuthorize("hasAnyRole('MAKER', 'ADMIN')")
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @Valid @RequestBody TransactionRequest request
    ) {

        return ResponseEntity.ok(
                transactionService.deposit(request)
        );
    }


    // ============================================================
    // WITHDRAW
    // ============================================================

    @PreAuthorize("hasAnyRole('MAKER', 'ADMIN')")
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @Valid @RequestBody TransactionRequest request
    ) {

        return ResponseEntity.ok(
                transactionService.withdraw(request)
        );
    }


    // ============================================================
    // TRANSFER
    // ============================================================

    @PreAuthorize("hasAnyRole('MAKER', 'ADMIN')")
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request
    ) {

        return ResponseEntity.ok(
                transactionService.transfer(request)
        );
    }


    // ============================================================
    // TRANSACTIONS BY ACCOUNT
    // ============================================================

    @PreAuthorize(
            "hasAnyRole('CUSTOMER', 'MAKER', 'CHECKER', 'ADMIN')"
    )
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponse>>
    getTransactionsByAccount(
            @PathVariable Long accountId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                transactionService.getTransactionsByAccount(
                        accountId,
                        authentication
                )
        );
    }


    // ============================================================
    // TRANSACTION BY ID
    // ============================================================

    @PreAuthorize(
            "hasAnyRole('CUSTOMER', 'MAKER', 'CHECKER', 'ADMIN')"
    )
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse>
    getTransactionById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                transactionService.getTransactionById(
                        id,
                        authentication
                )
        );
    }
}