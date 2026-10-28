package com.pesonary.ewalletapi.controller;

import com.pesonary.ewalletapi.dto.ApiResponse;
import com.pesonary.ewalletapi.dto.CustomerDTO;
import com.pesonary.ewalletapi.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * REST endpoints for customer profiles under {@code /api/v1/customers}.
 * <p>Handles HTTP only: reads parameters, runs {@code @Valid} on bodies and wraps results in {@link ApiResponse}. Access rules and logic live in {@link CustomerService}; exceptions go to {@code GlobalExceptionHandler}.
 *
 * @author Akaraan
 */
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerService customerService;

    /**
     * Creates the controller.
     * <p><b>Pre-conditions:</b> Spring injects every dependency.
     * <p><b>Post-conditions:</b> ready to handle requests.
     *
     * @param customerService customer business logic
     */
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Creates a customer. {@code POST /api/v1/customers}
     * <p><b>Pre-conditions:</b> the body passes validation; the caller is an admin.
     * <p><b>Post-conditions:</b> a new customer is saved.
     *
     * @param dto full name, email and phone
     * @return 201 response containing the new customer
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDTO>> createCustomer (@Valid @RequestBody CustomerDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Customer created", customerService.createCustomer(dto)));
    }

    /**
     * Returns one customer. {@code GET /api/v1/customers/{id}}
     * <p><b>Pre-conditions:</b> the caller is an admin.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param id customer's database ID
     * @return 200 response containing the customer
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDTO>> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Customer found", customerService.getCustomerById(id)));
    }

    /**
     * Lists every customer. {@code GET /api/v1/customers}
     * <p><b>Pre-conditions:</b> the caller is an admin.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @return 200 response containing the {@code List} of customers
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> getAllCustomers() {
        return ResponseEntity.ok(new ApiResponse<>(200, "Listing all customers", customerService.getAllCustomers()));
    }

    /**
     * Updates a customer. {@code PUT /api/v1/customers/{id}}
     * <p><b>Pre-conditions:</b> the body passes validation; the caller is an admin.
     * <p><b>Post-conditions:</b> the customer is updated.
     *
     * @param id customer's database ID
     * @param updatedDto new values
     * @return 200 response containing the updated customer
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDTO>> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerDTO updatedDto) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Customer updated successfully", customerService.updateCustomer(id, updatedDto)));
    }

    /**
     * Deletes a customer. {@code DELETE /api/v1/customers/{id}}
     * <p><b>Pre-conditions:</b> the caller is an admin.
     * <p><b>Post-conditions:</b> the customer is deleted.
     *
     * @param id customer's database ID
     * @return 200 response with the message {@code "Customer deleted"}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomerById(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Customer deleted"));
    }

    /**
     * Returns the logged-in user's own profile. {@code GET /api/v1/customers/me}
     * <p><b>Pre-conditions:</b> the caller is logged in.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param principal logged-in user, taken from the token
     * @return 200 response containing the user's profile
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CustomerDTO>> getMyProfile(Principal principal) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Profile retrieved",
                customerService.getMyProfile(principal.getName())));
    }
}
