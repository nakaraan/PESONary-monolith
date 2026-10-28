package com.pesonary.ewalletapi.controller;

import com.pesonary.ewalletapi.dto.AccountDTO;
import com.pesonary.ewalletapi.dto.ApiResponse;
import com.pesonary.ewalletapi.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for wallets under {@code /api/v1/accounts}.
 * <p>Handles HTTP only: reads parameters, runs {@code @Valid} on bodies and wraps results in {@link ApiResponse}. Access rules and logic live in {@link AccountService}; exceptions go to {@code GlobalExceptionHandler}.
 *
 * @author Akaraan
 */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    /**
     * Creates the controller.
     * <p><b>Pre-conditions:</b> Spring injects every dependency.
     * <p><b>Post-conditions:</b> ready to handle requests.
     *
     * @param accountService wallet business logic
     */
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Lists every wallet, or only those with a status. {@code GET /api/v1/accounts}
     * <p><b>Pre-conditions:</b> the caller is an admin.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param status optional filter: ACTIVE, FROZEN or CLOSED (any case); null lists every wallet
     * @return 200 response containing the {@code List} of wallets
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getAccounts(
            @RequestParam(required = false) String status) {
        List<AccountDTO> accounts = (status == null)
                ? accountService.getAllAccounts()
                : accountService.getAccountsByStatus(status);
        return ResponseEntity.ok(new ApiResponse<>(200, "Accounts retrieved", accounts));
    }

    /**
     * Lists wallets one page at a time. {@code GET /api/v1/accounts/page}
     * <p><b>Pre-conditions:</b> the caller is an admin.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param page zero-based page number (default 0)
     * @param size wallets per page (default 5)
     * @return 200 response containing the {@code Page} of wallets
     */
    @GetMapping("/page")
    public ResponseEntity<ApiResponse<Page<AccountDTO>>> getPaginatedAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Listing accounts",
                accountService.getPaginatedAccounts(page, size)));
    }

    /**
     * Opens a wallet. {@code POST /api/v1/accounts}
     * <p><b>Pre-conditions:</b> the body passes validation; the caller is an admin or the customer named by {@code customerId}.
     * <p><b>Post-conditions:</b> a new wallet is saved.
     *
     * @param dto wallet type, opening balance and owner's {@code customerId}
     * @return 201 response containing the new wallet
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AccountDTO>> createAccount(@Valid @RequestBody AccountDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Account created", accountService.createAccount(dto)));
    }

    /**
     * Returns one wallet. {@code GET /api/v1/accounts/{id}}
     * <p><b>Pre-conditions:</b> the caller is an admin or the owner.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param id wallet's database ID
     * @return 200 response containing the wallet
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDTO>> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Account found", accountService.getAccountById(id)));
    }


    /**
     * Updates a wallet. {@code PUT /api/v1/accounts/{id}}
     * <p><b>Pre-conditions:</b> the body passes validation; the caller is an admin.
     * <p><b>Post-conditions:</b> the wallet is updated.
     *
     * @param id wallet's database ID
     * @param updatedDto new values
     * @return 200 response containing the updated wallet
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDTO>> updateAccount(@PathVariable Long id, @Valid @RequestBody AccountDTO updatedDto) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Account updated successfully", accountService.updateAccount(id, updatedDto)));
    }

    /**
     * Deletes a wallet. {@code DELETE /api/v1/accounts/{id}}
     * <p><b>Pre-conditions:</b> the caller is an admin.
     * <p><b>Post-conditions:</b> the wallet is deleted.
     *
     * @param id wallet's database ID
     * @return 200 response with the message {@code "Account closed"}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> closeAccount(@PathVariable Long id) {
        accountService.closeAccount(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Account closed"));
    }
}
