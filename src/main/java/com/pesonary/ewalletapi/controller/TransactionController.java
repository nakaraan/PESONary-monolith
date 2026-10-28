package com.pesonary.ewalletapi.controller;

import com.pesonary.ewalletapi.dto.ApiResponse;
import com.pesonary.ewalletapi.dto.TransactionDTO;
import com.pesonary.ewalletapi.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionDTO>> createTransaction(@Valid @RequestBody TransactionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Transaction created", transactionService.createTransaction(dto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactionsByAccount(@RequestParam Long accountId) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Transactions retrieved", transactionService.getTransactionsByAccount(accountId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionDTO>> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Transaction found", transactionService.getTransactionById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionDTO>> updateTransaction(@PathVariable Long id, @Valid @RequestBody TransactionDTO updatedDto) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Transaction updated successfully", transactionService.updateTransaction(id, updatedDto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransactionById(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Transaction deleted"));
    }
}