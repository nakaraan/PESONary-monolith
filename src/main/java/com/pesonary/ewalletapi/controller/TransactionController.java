package com.pesonary.ewalletapi.controller;

import com.pesonary.ewalletapi.dto.ApiResponse;
import com.pesonary.ewalletapi.dto.TransactionDTO;
import com.pesonary.ewalletapi.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for the ledger under {@code /api/v1/transactions}.
 * <p>Handles HTTP only: reads parameters, runs {@code @Valid} on bodies and wraps results in {@link ApiResponse}. Access rules and logic live in {@link TransactionService}; exceptions go to {@code GlobalExceptionHandler}.
 *
 * @author Akaraan
 */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    /**
     * Creates the controller.
     * <p><b>Pre-conditions:</b> Spring injects every dependency.
     * <p><b>Post-conditions:</b> ready to handle requests.
     *
     * @param transactionService ledger business logic
     */
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Records a deposit, withdrawal or transfer. {@code POST /api/v1/transactions}
     * <p><b>Pre-conditions:</b> the body passes validation; the caller is an admin or owns the source wallet.
     * <p><b>Post-conditions:</b> balances are updated and the transaction is saved, or nothing changes if a rule refuses it.
     *
     * @param dto type, amount, source wallet and, for transfers, the target wallet
     * @return 201 response containing the saved transaction
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionDTO>> createTransaction(@Valid @RequestBody TransactionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Transaction created", transactionService.createTransaction(dto)));
    }

    /**
     * Lists a wallet's transactions, newest first. {@code GET /api/v1/transactions?accountId=}
     * <p><b>Pre-conditions:</b> the caller is an admin or owns the wallet.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param accountId ID of the source wallet (required)
     * @return 200 response containing the {@code List} of transactions
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactionsByAccount(@RequestParam Long accountId) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Transactions retrieved", transactionService.getTransactionsByAccount(accountId)));
    }

    /**
     * Returns one transaction. {@code GET /api/v1/transactions/{id}}
     * <p><b>Pre-conditions:</b> the caller is an admin or owns the source wallet.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param id transaction's database ID
     * @return 200 response containing the transaction
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionDTO>> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Transaction found", transactionService.getTransactionById(id)));
    }

    /**
     * Corrects a transaction's description. {@code PUT /api/v1/transactions/{id}}
     * <p><b>Pre-conditions:</b> the body passes validation; the caller is an admin.
     * <p><b>Post-conditions:</b> only the description changes.
     *
     * @param id transaction's database ID
     * @param updatedDto full transaction body; only the description is used
     * @return 200 response containing the updated transaction
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionDTO>> updateTransaction(@PathVariable Long id, @Valid @RequestBody TransactionDTO updatedDto) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Transaction updated successfully", transactionService.updateTransaction(id, updatedDto)));
    }

    /**
     * Deletes a transaction record. {@code DELETE /api/v1/transactions/{id}}
     * <p><b>Pre-conditions:</b> the caller is an admin.
     * <p><b>Post-conditions:</b> the record is deleted; balances are not reversed.
     *
     * @param id transaction's database ID
     * @return 200 response with the message {@code "Transaction deleted"}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransactionById(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Transaction deleted"));
    }
}