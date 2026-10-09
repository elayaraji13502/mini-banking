package com.banfico.minibanking.account.controller;

import com.banfico.minibanking.account.dto.BankAccountRequest;
import com.banfico.minibanking.account.dto.BankAccountResponse;
import com.banfico.minibanking.account.service.BankAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(
            BankAccountService bankAccountService
    ) {
        this.bankAccountService = bankAccountService;
    }

    /*
     * All authenticated banking roles can view accounts.
     *
     * CUSTOMER:
     *     Only own accounts are returned.
     *
     * MAKER/CHECKER/ADMIN:
     *     All accounts are returned.
     */
    @PreAuthorize(
            "hasAnyRole('CUSTOMER', 'MAKER', 'CHECKER', 'ADMIN')"
    )
    @GetMapping
    public ResponseEntity<List<BankAccountResponse>> getAllAccounts(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                bankAccountService.getAllAccounts(
                        authentication
                )
        );
    }

    /*
     * All authenticated banking roles can view an account.
     *
     * CUSTOMER:
     *     Only own account can be viewed.
     *
     * MAKER/CHECKER/ADMIN:
     *     Any account can be viewed.
     */
    @PreAuthorize(
            "hasAnyRole('CUSTOMER', 'MAKER', 'CHECKER', 'ADMIN')"
    )
    @GetMapping("/{id}")
    public ResponseEntity<BankAccountResponse> getAccountById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                bankAccountService.getAccountById(
                        id,
                        authentication
                )
        );
    }

    /*
     * All authenticated banking roles can view accounts
     * belonging to a customer.
     *
     * CUSTOMER:
     *     Only their own customer ID is allowed.
     *
     * MAKER/CHECKER/ADMIN:
     *     Any customer ID is allowed.
     */
    @PreAuthorize(
            "hasAnyRole('CUSTOMER', 'MAKER', 'CHECKER', 'ADMIN')"
    )
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BankAccountResponse>>
    getAccountsByCustomer(
            @PathVariable Long customerId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                bankAccountService.getAccountsByCustomer(
                        customerId,
                        authentication
                )
        );
    }

    /*
     * Only ADMIN can create an account.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<BankAccountResponse> createAccount(
            @Valid @RequestBody BankAccountRequest request
    ) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        bankAccountService.createAccount(request)
                );
    }

    /*
     * Only ADMIN can block an account.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/block")
    public ResponseEntity<BankAccountResponse> blockAccount(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                bankAccountService.blockAccount(id)
        );
    }

    /*
     * Only ADMIN can activate an account.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<BankAccountResponse> activateAccount(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                bankAccountService.activateAccount(id)
        );
    }

    /*
     * Only ADMIN can close an account.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/close")
    public ResponseEntity<BankAccountResponse> closeAccount(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                bankAccountService.closeAccount(id)
        );
    }
}
