package com.pesonary.ewalletapi.controller;

import com.pesonary.ewalletapi.dto.AccountDTO;
import com.pesonary.ewalletapi.exception.ResourceNotFoundException;
import com.pesonary.ewalletapi.security.CustomUserDetailsService;
import com.pesonary.ewalletapi.security.JwtUtil;
import com.pesonary.ewalletapi.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice tests with {@code @WebMvcTest} and MockMvc for {@link AccountController}.
 * <p>Each test method is one documented case in {@code docs/PESONary Test Cases - Almin, Wesner III C..pdf} ({@code TC-AC-01} to {@code TC-AC-18}); the test method name is the trace key.
 * <p>The service is mocked and filters are off ({@code addFilters = false}), so these tests check status codes, JSON, validation and the exception mapping. {@code JwtUtil} and {@code CustomUserDetailsService} are mocked only because the JWT filter bean needs them.
 *
 * @author Akaraan
 */
@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean AccountService accountService;
    // JwtAuthenticationFilter is a Filter bean, so the slice tries to build it
    @MockitoBean JwtUtil jwtUtil;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    private static final String VALID_BODY = """
            {"accountType":"SAVINGS","balance":1000.00,"customerId":1}
            """;
    private static final String NEGATIVE_BALANCE_BODY = """
            {"accountType":"SAVINGS","balance":-5.00,"customerId":1}
            """;
    private static final String BLANK_TYPE_BODY = """
            {"accountType":"","balance":10.00,"customerId":1}
            """;

    /**
     * Builds the wallet the mocked service returns.
     * <p><b>Pre-conditions:</b> none.
     * <p><b>Post-conditions:</b> returns a new object; nothing else changes.
     *
     * @param id wallet ID
     * @param status wallet status
     * @return AccountDTO object with type SAVINGS, ₱1,000.00, tier BASIC
     */
    private AccountDTO accountDto(Long id, String status) {
        AccountDTO dto = new AccountDTO();
        dto.setId(id);
        dto.setAccountType("SAVINGS");
        dto.setBalance(new BigDecimal("1000.00"));
        dto.setStatus(status);
        dto.setTier("BASIC");
        return dto;
    }

    // GET /accounts
    /**
     * TC-AC-01 (Success): GET /api/v1/accounts.
     * <p>Expect: 200; 2 accounts in data; status filter not used.
     */
    @Test
    void getAccounts_noFilter_returnsAll() throws Exception {
        when(accountService.getAllAccounts()).thenReturn(List.of(accountDto(1L, "ACTIVE"), accountDto(2L, "FROZEN")));

        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
        verify(accountService, never()).getAccountsByStatus(any());
    }

    /**
     * TC-AC-02 (Success): GET /api/v1/accounts?status=FROZEN.
     * <p>Expect: 200; data[0].status FROZEN; full list not used.
     */
    @Test
    void getAccounts_withStatus_filters() throws Exception {
        when(accountService.getAccountsByStatus("FROZEN")).thenReturn(List.of(accountDto(2L, "FROZEN")));

        mockMvc.perform(get("/api/v1/accounts").param("status", "FROZEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("FROZEN"));
        verify(accountService, never()).getAllAccounts();
    }

    /**
     * TC-AC-03 (Failure): ?status=BOGUS (service throws IllegalArgumentException).
     * <p>Expect: 400.
     */
    @Test
    void getAccounts_badStatus() throws Exception {
        when(accountService.getAccountsByStatus("BOGUS")).thenThrow(new IllegalArgumentException("No enum constant"));

        mockMvc.perform(get("/api/v1/accounts").param("status", "BOGUS"))
                .andExpect(status().isBadRequest());
    }

    // GET /accounts/page
    /**
     * TC-AC-04 (Success): GET /api/v1/accounts/page, no parameters.
     * <p>Expect: 200; service called with page 0, size 5.
     */
    @Test
    void getPaginatedAccounts_defaultsTo0And5() throws Exception {
        when(accountService.getPaginatedAccounts(0, 5)).thenReturn(
                new PageImpl<>(List.of(accountDto(1L, "ACTIVE")), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/api/v1/accounts/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    /**
     * TC-AC-05 (Edge): ?page=2&amp;size=10.
     * <p>Expect: 200; service called with page 2, size 10.
     */
    @Test
    void getPaginatedAccounts_customPageAndSize() throws Exception {
        when(accountService.getPaginatedAccounts(2, 10)).thenReturn(
                new PageImpl<>(List.of(), PageRequest.of(2, 10), 0));

        mockMvc.perform(get("/api/v1/accounts/page").param("page", "2").param("size", "10"))
                .andExpect(status().isOk());
        verify(accountService).getPaginatedAccounts(2, 10);
    }

    /**
     * TC-AC-06 (Failure): ?page=abc.
     * <p>Expect: 400.
     */
    @Test
    void getPaginatedAccounts_nonNumericPage() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/page").param("page", "abc"))
                .andExpect(status().isBadRequest());
    }

    // POST /accounts
    /**
     * TC-AC-07 (Success): POST /api/v1/accounts: SAVINGS, PHP 1,000.00, customerId 1.
     * <p>Expect: 201; data.id 1.
     */
    @Test
    void createAccount_created() throws Exception {
        when(accountService.createAccount(any(AccountDTO.class))).thenReturn(accountDto(1L, "ACTIVE"));

        mockMvc.perform(post("/api/v1/accounts").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    /**
     * TC-AC-08 (Failure): accountType "".
     * <p>Expect: 400; service not called.
     */
    @Test
    void createAccount_blankType() throws Exception {
        mockMvc.perform(post("/api/v1/accounts").contentType(MediaType.APPLICATION_JSON).content(BLANK_TYPE_BODY))
                .andExpect(status().isBadRequest());
        verify(accountService, never()).createAccount(any(AccountDTO.class));
    }

    /**
     * TC-AC-09 (Failure): valid body; service throws ResourceNotFoundException.
     * <p>Expect: 404.
     */
    @Test
    void createAccount_customerNotFound() throws Exception {
        when(accountService.createAccount(any(AccountDTO.class))).thenThrow(new ResourceNotFoundException("Customer not found"));

        mockMvc.perform(post("/api/v1/accounts").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound());
    }

    // GET /accounts/{id}
    /**
     * TC-AC-10 (Success): GET /api/v1/accounts/1.
     * <p>Expect: 200; data.id 1.
     */
    @Test
    void getAccountById_found() throws Exception {
        when(accountService.getAccountById(1L)).thenReturn(accountDto(1L, "ACTIVE"));

        mockMvc.perform(get("/api/v1/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    /**
     * TC-AC-11 (Failure): GET /api/v1/accounts/99.
     * <p>Expect: 404; message "Account not found".
     */
    @Test
    void getAccountById_notFound() throws Exception {
        when(accountService.getAccountById(99L)).thenThrow(new ResourceNotFoundException("Account not found"));

        mockMvc.perform(get("/api/v1/accounts/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found"));
    }

    /**
     * TC-AC-12 (Edge): GET /api/v1/accounts/abc.
     * <p>Expect: 400.
     */
    @Test
    void getAccountById_nonNumericId() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/abc"))
                .andExpect(status().isBadRequest());
    }

    // PUT /accounts/{id}
    /**
     * TC-AC-13 (Success): PUT /api/v1/accounts/1, valid body.
     * <p>Expect: 200; data.status FROZEN.
     */
    @Test
    void updateAccount_ok() throws Exception {
        when(accountService.updateAccount(eq(1L), any(AccountDTO.class))).thenReturn(accountDto(1L, "FROZEN"));

        mockMvc.perform(put("/api/v1/accounts/1").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FROZEN"));
    }

    /**
     * TC-AC-14 (Failure): PUT /api/v1/accounts/99.
     * <p>Expect: 404.
     */
    @Test
    void updateAccount_notFound() throws Exception {
        when(accountService.updateAccount(eq(99L), any(AccountDTO.class)))
                .thenThrow(new ResourceNotFoundException("Account not found"));

        mockMvc.perform(put("/api/v1/accounts/99").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound());
    }

    /**
     * TC-AC-15 (Edge): balance -5.00.
     * <p>Expect: 400.
     */
    @Test
    void updateAccount_negativeBalance() throws Exception {
        mockMvc.perform(put("/api/v1/accounts/1").contentType(MediaType.APPLICATION_JSON).content(NEGATIVE_BALANCE_BODY))
                .andExpect(status().isBadRequest());
    }

    // DELETE /accounts/{id}
    /**
     * TC-AC-16 (Success): DELETE /api/v1/accounts/1.
     * <p>Expect: 200; message "Account closed"; service called with id 1.
     */
    @Test
    void closeAccount_ok() throws Exception {
        mockMvc.perform(delete("/api/v1/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account closed"));
        verify(accountService).closeAccount(1L);
    }

    /**
     * TC-AC-17 (Failure): DELETE /api/v1/accounts/99.
     * <p>Expect: 404.
     */
    @Test
    void closeAccount_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Account not found")).when(accountService).closeAccount(99L);

        mockMvc.perform(delete("/api/v1/accounts/99"))
                .andExpect(status().isNotFound());
    }

    /**
     * TC-AC-18 (Edge): DELETE /api/v1/accounts/abc.
     * <p>Expect: 400.
     */
    @Test
    void closeAccount_nonNumericId() throws Exception {
        mockMvc.perform(delete("/api/v1/accounts/abc"))
                .andExpect(status().isBadRequest());
    }
}