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

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getAccounts(
            @RequestParam(required = false) String status) {
        List<AccountDTO> accounts = (status == null)
                ? accountService.getAllAccounts()
                : accountService.getAccountsByStatus(status);
        return ResponseEntity.ok(new ApiResponse<>(200, "Accounts retrieved", accounts));
    }

    @GetMapping("/page")
    public ResponseEntity<ApiResponse<Page<AccountDTO>>> getPaginatedAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Listing accounts",
                accountService.getPaginatedAccounts(page, size)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountDTO>> createAccount(@Valid @RequestBody AccountDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Account created", accountService.createAccount(dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDTO>> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Account found", accountService.getAccountById(id)));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDTO>> updateAccount(@PathVariable Long id, @Valid @RequestBody AccountDTO updatedDto) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Account updated successfully", accountService.updateAccount(id, updatedDto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> closeAccount(@PathVariable Long id) {
        accountService.closeAccount(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Account closed"));
    }
}
