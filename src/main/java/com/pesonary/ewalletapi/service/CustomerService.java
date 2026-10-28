package com.pesonary.ewalletapi.service;

import com.pesonary.ewalletapi.dto.CustomerDTO;
import com.pesonary.ewalletapi.entity.Customer;
import com.pesonary.ewalletapi.entity.User;
import com.pesonary.ewalletapi.exception.BusinessRuleException;
import com.pesonary.ewalletapi.exception.ResourceNotFoundException;
import com.pesonary.ewalletapi.repository.CustomerRepository;
import com.pesonary.ewalletapi.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for customer profiles: create, read, update, delete, and the logged-in user's own profile.
 * <p>Every public method takes and returns DTOs; entities never leave this class. Access rules sit on each method ({@code @PreAuthorize}, {@code @PostAuthorize}). Missing records throw {@link ResourceNotFoundException}; refused rules throw {@link BusinessRuleException}.
 *
 * @author Akaraan
 */
@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    /**
     * Creates the service.
     * <p><b>Pre-conditions:</b> Spring injects every dependency.
     * <p><b>Post-conditions:</b> ready to use.
     *
     * @param customerRepository data access for customers
     * @param userRepository data access for users, used by {@code getMyProfile}
     */
    public CustomerService(CustomerRepository customerRepository, UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    /**
     * Returns one customer by ID.
     * <p><b>Pre-conditions:</b> {@code id} is not null; the caller is an admin.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param id customer's database ID
     * @return CustomerDTO object containing the customer
     * @throws ResourceNotFoundException if no customer has this ID
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public CustomerDTO getCustomerById(Long id) {
        return customerRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    /**
     * Returns the logged-in user's own customer profile.
     * <p><b>Pre-conditions:</b> {@code username} comes from the logged-in principal.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param username logged-in username
     * @return CustomerDTO object containing the user's profile
     * @throws ResourceNotFoundException if the user does not exist or has no customer profile
     */
    @Transactional(readOnly = true)
    public CustomerDTO getMyProfile(String username) {
        return userRepository.findByUsername(username)
                .map(User::getCustomer)          // null customer → empty Optional
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    /**
     * Lists every customer.
     * <p><b>Pre-conditions:</b> the caller is an admin.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @return {@code List} of every customer; empty if none
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * Creates a customer profile without a login account.
     * <p><b>Pre-conditions:</b> {@code dto} passed validation; the caller is an admin.
     * <p><b>Post-conditions:</b> a new customer is saved.
     *
     * @param dto full name, email and phone
     * @return CustomerDTO object containing the saved customer
     * @throws BusinessRuleException if another customer already has this email
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CustomerDTO createCustomer(CustomerDTO dto) {
        if (customerRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessRuleException("Email is already in use");
        }
        Customer customer = new Customer();
        customer.setFullName(dto.getFullName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        return convertToDTO(customerRepository.save(customer));
    }

    /**
     * Updates a customer's name, email and phone.
     * <p><b>Pre-conditions:</b> {@code dto} passed validation; the caller is an admin.
     * <p><b>Post-conditions:</b> the three fields are replaced.
     *
     * @param id customer's database ID
     * @param dto new values
     * @return CustomerDTO object containing the updated customer
     * @throws ResourceNotFoundException if no customer has this ID
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CustomerDTO updateCustomer(Long id, CustomerDTO dto) {
        Customer customer = findOrThrow(id);
        customer.setFullName(dto.getFullName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        return convertToDTO(customerRepository.save(customer));
    }

    /**
     * Deletes a customer.
     * <p><b>Pre-conditions:</b> the caller is an admin.
     * <p><b>Post-conditions:</b> the customer is deleted; a customer who still has wallets or a login account fails on the foreign key (409).
     *
     * @param id customer's database ID
     * @throws ResourceNotFoundException if no customer has this ID
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCustomer(Long id) {
        customerRepository.delete(findOrThrow(id));
    }

    /**
     * Loads a customer or throws.
     * <p><b>Pre-conditions:</b> {@code id} is not null.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param id customer's database ID
     * @return Customer entity
     * @throws ResourceNotFoundException if no customer has this ID
     */
    private Customer findOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    /**
     * Maps a customer entity to its DTO.
     * <p><b>Pre-conditions:</b> {@code customer} is not null.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param customer customer entity
     * @return CustomerDTO object containing the profile and, if linked, the username
     */
    private CustomerDTO convertToDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setEmail(customer.getEmail());
        dto.setFullName(customer.getFullName());
        dto.setPhone(customer.getPhone());
        if (customer.getUser() != null) {
            dto.setUsername(customer.getUser().getUsername());
        }
        return dto;
    }
}
