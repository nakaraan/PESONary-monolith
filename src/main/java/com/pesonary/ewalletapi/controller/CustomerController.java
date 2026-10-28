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

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDTO>> createCustomer (@Valid @RequestBody CustomerDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Customer created", customerService.createCustomer(dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDTO>> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Customer found", customerService.getCustomerById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> getAllCustomers() {
        return ResponseEntity.ok(new ApiResponse<>(200, "Listing all customers", customerService.getAllCustomers()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDTO>> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerDTO updatedDto) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Customer updated successfully", customerService.updateCustomer(id, updatedDto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDTO>> deleteCustomerById(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Customer deleted"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CustomerDTO>> getMyProfile(Principal principal) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Profile retrieved",
                customerService.getMyProfile(principal.getName())));
    }
}
