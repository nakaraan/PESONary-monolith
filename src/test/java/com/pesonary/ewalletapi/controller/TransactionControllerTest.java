package com.pesonary.ewalletapi.controller;

import com.pesonary.ewalletapi.dto.TransactionDTO;
import com.pesonary.ewalletapi.exception.BusinessRuleException;
import com.pesonary.ewalletapi.exception.ResourceNotFoundException;
import com.pesonary.ewalletapi.security.CustomUserDetailsService;
import com.pesonary.ewalletapi.security.JwtUtil;
import com.pesonary.ewalletapi.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice tests with {@code @WebMvcTest} and MockMvc for {@link TransactionController}.
 * <p>Each test method is one documented case in {@code docs/TEST_CASES.docx} ({@code TC-TC-01} to {@code TC-TC-15}); the test method name is the trace key.
 * <p>The service is mocked and filters are off ({@code addFilters = false}), so these tests check status codes, JSON, validation and the exception mapping. {@code JwtUtil} and {@code CustomUserDetailsService} are mocked only because the JWT filter bean needs them.
 *
 * @author Akaraan
 */
@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean TransactionService transactionService;
    // JwtAuthenticationFilter is a Filter bean, so the slice tries to build it
    @MockitoBean JwtUtil jwtUtil;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    private static final String VALID_BODY = """
            {"type":"DEPOSIT","amount":100.00,"accountId":1,"description":"salary"}
            """;
    private static final String ZERO_AMOUNT_BODY = """
            {"type":"DEPOSIT","amount":0,"accountId":1}
            """;
    private static final String BAD_TYPE_BODY = """
            {"type":"REFUND","amount":10.00,"accountId":1}
            """;

    /**
     * Builds the transaction the mocked service returns.
     * <p><b>Pre-conditions:</b> none.
     * <p><b>Post-conditions:</b> returns a new object; nothing else changes.
     *
     * @param id transaction ID
     * @return TransactionDTO object: DEPOSIT, ₱100.00, account 1
     */
    private TransactionDTO txDto(Long id) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(id);
        dto.setType("DEPOSIT");
        dto.setAmount(new BigDecimal("100.00"));
        dto.setAccountId(1L);
        return dto;
    }

    // POST /transactions
    /**
     * TC-TC-01 (Success): POST /api/v1/transactions: DEPOSIT PHP 100.00, account 1.
     * <p>Expect: 201; data.id 10.
     */
    @Test
    void createTransaction_created() throws Exception {
        when(transactionService.createTransaction(any(TransactionDTO.class))).thenReturn(txDto(10L));

        mockMvc.perform(post("/api/v1/transactions").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(10));
    }

    /**
     * TC-TC-02 (Failure): amount 0.
     * <p>Expect: 400; service not called.
     */
    @Test
    void createTransaction_zeroAmount() throws Exception {
        mockMvc.perform(post("/api/v1/transactions").contentType(MediaType.APPLICATION_JSON).content(ZERO_AMOUNT_BODY))
                .andExpect(status().isBadRequest());
        verify(transactionService, never()).createTransaction(any(TransactionDTO.class));
    }

    /**
     * TC-TC-03 (Failure): service throws BusinessRuleException "Insufficient funds".
     * <p>Expect: 422 "Insufficient funds".
     */
    @Test
    void createTransaction_businessRule_returns422() throws Exception {
        when(transactionService.createTransaction(any(TransactionDTO.class)))
                .thenThrow(new BusinessRuleException("Insufficient funds"));

        mockMvc.perform(post("/api/v1/transactions").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Insufficient funds"));
    }

    // GET /transactions?accountId=
    /**
     * TC-TC-04 (Success): GET /api/v1/transactions?accountId=1; 2 transactions.
     * <p>Expect: 200; 2 transactions in data.
     */
    @Test
    void getTransactionsByAccount_ok() throws Exception {
        when(transactionService.getTransactionsByAccount(1L)).thenReturn(List.of(txDto(10L), txDto(11L)));

        mockMvc.perform(get("/api/v1/transactions").param("accountId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    /**
     * TC-TC-05 (Edge): ?accountId=1; no transactions.
     * <p>Expect: 200; empty data.
     */
    @Test
    void getTransactionsByAccount_empty() throws Exception {
        when(transactionService.getTransactionsByAccount(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/transactions").param("accountId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    /**
     * TC-TC-06 (Failure): GET /api/v1/transactions without accountId.
     * <p>Expect: 400.
     */
    @Test
    void getTransactionsByAccount_missingAccountId() throws Exception {
        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isBadRequest());
    }

    // GET /transactions/{id}
    /**
     * TC-TC-07 (Success): GET /api/v1/transactions/10.
     * <p>Expect: 200; data.id 10.
     */
    @Test
    void getTransactionById_found() throws Exception {
        when(transactionService.getTransactionById(10L)).thenReturn(txDto(10L));

        mockMvc.perform(get("/api/v1/transactions/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10));
    }

    /**
     * TC-TC-08 (Failure): GET /api/v1/transactions/99.
     * <p>Expect: 404 "Transaction not found".
     */
    @Test
    void getTransactionById_notFound() throws Exception {
        when(transactionService.getTransactionById(99L)).thenThrow(new ResourceNotFoundException("Transaction not found"));

        mockMvc.perform(get("/api/v1/transactions/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction not found"));
    }

    /**
     * TC-TC-09 (Edge): GET /api/v1/transactions/abc.
     * <p>Expect: 400.
     */
    @Test
    void getTransactionById_nonNumericId() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/abc"))
                .andExpect(status().isBadRequest());
    }

    // PUT /transactions/{id}
    /**
     * TC-TC-10 (Success): PUT /api/v1/transactions/10, valid body.
     * <p>Expect: 200.
     */
    @Test
    void updateTransaction_ok() throws Exception {
        when(transactionService.updateTransaction(eq(10L), any(TransactionDTO.class))).thenReturn(txDto(10L));

        mockMvc.perform(put("/api/v1/transactions/10").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk());
    }

    /**
     * TC-TC-11 (Failure): PUT /api/v1/transactions/99.
     * <p>Expect: 404.
     */
    @Test
    void updateTransaction_notFound() throws Exception {
        when(transactionService.updateTransaction(eq(99L), any(TransactionDTO.class)))
                .thenThrow(new ResourceNotFoundException("Transaction not found"));

        mockMvc.perform(put("/api/v1/transactions/99").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound());
    }

    /**
     * TC-TC-12 (Edge): type "REFUND".
     * <p>Expect: 400.
     */
    @Test
    void updateTransaction_badType() throws Exception {
        mockMvc.perform(put("/api/v1/transactions/10").contentType(MediaType.APPLICATION_JSON).content(BAD_TYPE_BODY))
                .andExpect(status().isBadRequest());
    }

    // DELETE /transactions/{id}
    /**
     * TC-TC-13 (Success): DELETE /api/v1/transactions/10.
     * <p>Expect: 200 "Transaction deleted"; service called with id 10.
     */
    @Test
    void deleteTransaction_ok() throws Exception {
        mockMvc.perform(delete("/api/v1/transactions/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted"));
        verify(transactionService).deleteTransaction(10L);
    }

    /**
     * TC-TC-14 (Failure): DELETE /api/v1/transactions/99.
     * <p>Expect: 404.
     */
    @Test
    void deleteTransaction_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Transaction not found")).when(transactionService).deleteTransaction(99L);

        mockMvc.perform(delete("/api/v1/transactions/99"))
                .andExpect(status().isNotFound());
    }

    /**
     * TC-TC-15 (Edge): DELETE /api/v1/transactions/abc.
     * <p>Expect: 400.
     */
    @Test
    void deleteTransaction_nonNumericId() throws Exception {
        mockMvc.perform(delete("/api/v1/transactions/abc"))
                .andExpect(status().isBadRequest());
    }
}