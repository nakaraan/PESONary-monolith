package com.pesonary.ewalletapi.controller;

import com.pesonary.ewalletapi.dto.CustomerDTO;
import com.pesonary.ewalletapi.exception.BusinessRuleException;
import com.pesonary.ewalletapi.exception.ResourceNotFoundException;
import com.pesonary.ewalletapi.security.CustomUserDetailsService;
import com.pesonary.ewalletapi.security.JwtUtil;
import com.pesonary.ewalletapi.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice tests with {@code @WebMvcTest} and MockMvc for {@link CustomerController}.
 * <p>Each test method is one documented case in {@code docs/PESONary Test Cases - Almin, Wesner III C..pdf} ({@code TC-CC-01} to {@code TC-CC-18}); the test method name is the trace key.
 * <p>The service is mocked and filters are off ({@code addFilters = false}), so these tests check status codes, JSON, validation and the exception mapping. {@code JwtUtil} and {@code CustomUserDetailsService} are mocked only because the JWT filter bean needs them.
 *
 * @author Akaraan
 */
@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean CustomerService customerService;
    // JwtAuthenticationFilter is a Filter bean, so the slice tries to build it
    @MockitoBean JwtUtil jwtUtil;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    private static final String VALID_BODY = """
            {"fullName":"Juan Dela Cruz","email":"juan@example.com","phone":"09171234567"}
            """;
    private static final String INVALID_BODY = """
            {"fullName":"","email":"not-an-email","phone":"123"}
            """;

    /**
     * Builds the customer the mocked service returns.
     * <p><b>Pre-conditions:</b> none.
     * <p><b>Post-conditions:</b> returns a new object; nothing else changes.
     *
     * @param id customer ID
     * @param name full name; the email is derived from it
     * @return CustomerDTO object
     */
    private CustomerDTO customerDto(Long id, String name) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(id);
        dto.setFullName(name);
        dto.setEmail(name.toLowerCase().replace(" ", ".") + "@example.com");
        return dto;
    }

    // POST /customers
    /**
     * TC-CC-01 (Success): POST /api/v1/customers: Juan Dela Cruz, juan@example.com, 09171234567.
     * <p>Expect: 201; message "Customer created"; data.fullName "Juan Dela Cruz".
     */
    @Test
    void createCustomer_created() throws Exception {
        when(customerService.createCustomer(any(CustomerDTO.class))).thenReturn(customerDto(1L, "Juan Dela Cruz"));

        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Customer created"))
                .andExpect(jsonPath("$.data.fullName").value("Juan Dela Cruz"));
    }

    /**
     * TC-CC-02 (Failure): blank name, "not-an-email", phone "123".
     * <p>Expect: 400.
     */
    @Test
    void createCustomer_invalidBody() throws Exception {
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON).content(INVALID_BODY))
                .andExpect(status().isBadRequest());
    }

    /**
     * TC-CC-03 (Failure): valid body; service throws BusinessRuleException.
     * <p>Expect: 422 "Email is already in use".
     */
    @Test
    void createCustomer_duplicateEmail() throws Exception {
        when(customerService.createCustomer(any(CustomerDTO.class)))
                .thenThrow(new BusinessRuleException("Email is already in use"));

        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message").value("Email is already in use"));
    }

    // GET /customers/{id}
    /**
     * TC-CC-04 (Success): GET /api/v1/customers/1.
     * <p>Expect: 200; data.id 1.
     */
    @Test
    void getCustomerById_found() throws Exception {
        when(customerService.getCustomerById(1L)).thenReturn(customerDto(1L, "Juan Dela Cruz"));

        mockMvc.perform(get("/api/v1/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    /**
     * TC-CC-05 (Failure): GET /api/v1/customers/99.
     * <p>Expect: 404 "Customer not found".
     */
    @Test
    void getCustomerById_notFound() throws Exception {
        when(customerService.getCustomerById(99L)).thenThrow(new ResourceNotFoundException("Customer not found"));

        mockMvc.perform(get("/api/v1/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }

    /**
     * TC-CC-06 (Edge): GET /api/v1/customers/abc.
     * <p>Expect: 400.
     */
    @Test
    void getCustomerById_nonNumericId() throws Exception {
        mockMvc.perform(get("/api/v1/customers/abc"))
                .andExpect(status().isBadRequest());
    }

    // GET /customers
    /**
     * TC-CC-07 (Success): GET /api/v1/customers; service returns 2.
     * <p>Expect: 200; 2 customers in data.
     */
    @Test
    void getAllCustomers_two() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of(customerDto(1L, "Juan Dela Cruz"), customerDto(2L, "Maria Clara")));

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    /**
     * TC-CC-08 (Edge): GET /api/v1/customers; service returns none.
     * <p>Expect: 200; empty data.
     */
    @Test
    void getAllCustomers_empty() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    /**
     * TC-CC-09 (Success): GET /api/v1/customers.
     * <p>Expect: message "Listing all customers".
     */
    @Test
    void getAllCustomers_message() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(jsonPath("$.message").value("Listing all customers"));
    }

    // PUT /customers/{id}
    /**
     * TC-CC-10 (Success): PUT /api/v1/customers/1, valid body.
     * <p>Expect: 200; data.fullName "Juan Updated".
     */
    @Test
    void updateCustomer_ok() throws Exception {
        when(customerService.updateCustomer(eq(1L), any(CustomerDTO.class))).thenReturn(customerDto(1L, "Juan Updated"));

        mockMvc.perform(put("/api/v1/customers/1").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Juan Updated"));
    }

    /**
     * TC-CC-11 (Failure): PUT /api/v1/customers/99.
     * <p>Expect: 404.
     */
    @Test
    void updateCustomer_notFound() throws Exception {
        when(customerService.updateCustomer(eq(99L), any(CustomerDTO.class)))
                .thenThrow(new ResourceNotFoundException("Customer not found"));

        mockMvc.perform(put("/api/v1/customers/99").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound());
    }

    /**
     * TC-CC-12 (Edge): PUT with blank name, invalid email and phone.
     * <p>Expect: 400.
     */
    @Test
    void updateCustomer_invalidBody() throws Exception {
        mockMvc.perform(put("/api/v1/customers/1").contentType(MediaType.APPLICATION_JSON).content(INVALID_BODY))
                .andExpect(status().isBadRequest());
    }

    // DELETE /customers/{id}
    /**
     * TC-CC-13 (Success): DELETE /api/v1/customers/1.
     * <p>Expect: 200 "Customer deleted"; service called with id 1.
     */
    @Test
    void deleteCustomer_ok() throws Exception {
        mockMvc.perform(delete("/api/v1/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer deleted"));

        verify(customerService).deleteCustomer(1L);
    }

    /**
     * TC-CC-14 (Failure): DELETE /api/v1/customers/99.
     * <p>Expect: 404.
     */
    @Test
    void deleteCustomer_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Customer not found")).when(customerService).deleteCustomer(99L);

        mockMvc.perform(delete("/api/v1/customers/99"))
                .andExpect(status().isNotFound());
    }

    /**
     * TC-CC-15 (Edge): DELETE /api/v1/customers/abc.
     * <p>Expect: 400.
     */
    @Test
    void deleteCustomer_nonNumericId() throws Exception {
        mockMvc.perform(delete("/api/v1/customers/abc"))
                .andExpect(status().isBadRequest());
    }

    // GET /customers/me
    /**
     * TC-CC-16 (Success): GET /api/v1/customers/me as alice.
     * <p>Expect: 200; data.fullName "Alice Cruz".
     */
    @Test
    void getMyProfile_ok() throws Exception {
        when(customerService.getMyProfile("alice")).thenReturn(customerDto(1L, "Alice Cruz"));

        mockMvc.perform(get("/api/v1/customers/me").principal(() -> "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Alice Cruz"));
    }

    /**
     * TC-CC-17 (Failure): GET /api/v1/customers/me as ghost (no profile).
     * <p>Expect: 404.
     */
    @Test
    void getMyProfile_notFound() throws Exception {
        when(customerService.getMyProfile("ghost")).thenThrow(new ResourceNotFoundException("Customer not found"));

        mockMvc.perform(get("/api/v1/customers/me").principal(() -> "ghost"))
                .andExpect(status().isNotFound());
    }

    /**
     * TC-CC-18 (Edge): GET /api/v1/customers/me as alice.
     * <p>Expect: service called with "alice" from the principal.
     */
    @Test
    void getMyProfile_usesLoggedInUsername() throws Exception {
        when(customerService.getMyProfile("alice")).thenReturn(customerDto(1L, "Alice Cruz"));

        mockMvc.perform(get("/api/v1/customers/me").principal(() -> "alice"));

        verify(customerService).getMyProfile("alice");
    }
}