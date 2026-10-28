package com.pesonary.ewalletapi.service;

import com.pesonary.ewalletapi.dto.CustomerDTO;
import com.pesonary.ewalletapi.entity.Customer;
import com.pesonary.ewalletapi.entity.User;
import com.pesonary.ewalletapi.exception.BusinessRuleException;
import com.pesonary.ewalletapi.exception.ResourceNotFoundException;
import com.pesonary.ewalletapi.repository.CustomerRepository;
import com.pesonary.ewalletapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

/**
 * Unit tests with Mockito for {@link CustomerService}.
 * <p>Each test method is one documented case in {@code docs/PESONary Test Cases - Almin, Wesner III C..pdf} ({@code TC-CS-01} to {@code TC-CS-18}); the test method name is the trace key.
 * <p>Repositories are mocked, so there is no database or Spring context; method security does not run here.
 *
 * @author Akaraan
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock CustomerRepository customerRepository;
    @Mock UserRepository userRepository;
    @InjectMocks CustomerService customerService;

    /**
     * Builds a customer entity for test setup.
     * <p><b>Pre-conditions:</b> none.
     * <p><b>Post-conditions:</b> returns a new object; nothing else changes.
     *
     * @param name full name
     * @param email email address
     * @return Customer entity
     */
    private Customer customer(String name, String email) {
        Customer c = new Customer();
        c.setFullName(name);
        c.setEmail(email);
        return c;
    }

    /**
     * Builds a customer request.
     * <p><b>Pre-conditions:</b> none.
     * <p><b>Post-conditions:</b> returns a new object; nothing else changes.
     *
     * @param name full name
     * @param email email address
     * @param phone mobile number; may be null
     * @return CustomerDTO object
     */
    private CustomerDTO dto(String name, String email, String phone) {
        CustomerDTO d = new CustomerDTO();
        d.setFullName(name);
        d.setEmail(email);
        d.setPhone(phone);
        return d;
    }

    // createCustomer
    /**
     * TC-CS-01 (Success): new email a@x.com; Alice, 09171234567.
     * <p>Expect: fullName "Alice", phone 09171234567.
     */
    @Test
    void createCustomer_savesAndReturns() {
        when(customerRepository.existsByEmail("a@x.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(returnsFirstArg());

        CustomerDTO result = customerService.createCustomer(dto("Alice", "a@x.com", "09171234567"));

        assertEquals("Alice", result.getFullName());
        assertEquals("09171234567", result.getPhone());
    }

    /**
     * TC-CS-02 (Failure): email a@x.com already in use.
     * <p>Expect: BusinessRuleException "Email is already in use".
     */
    @Test
    void createCustomer_duplicateEmail_throws() {
        when(customerRepository.existsByEmail("a@x.com")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> customerService.createCustomer(dto("Alice", "a@x.com", null)));

        assertEquals("Email is already in use", ex.getMessage());
    }

    /**
     * TC-CS-03 (Edge): email a@x.com already in use.
     * <p>Expect: save is never called.
     */
    @Test
    void createCustomer_duplicateEmail_doesNotSave() {
        when(customerRepository.existsByEmail("a@x.com")).thenReturn(true);

        assertThrows(BusinessRuleException.class,
                () -> customerService.createCustomer(dto("Alice", "a@x.com", null)));

        verify(customerRepository, never()).save(any(Customer.class));
    }

    // getAllCustomers
    /**
     * TC-CS-04 (Success): repository holds 2 customers.
     * <p>Expect: list of 2 customers.
     */
    @Test
    void getAllCustomers_returnsAll() {
        when(customerRepository.findAll()).thenReturn(List.of(customer("A", "a@x.com"), customer("B", "b@x.com")));

        assertEquals(2, customerService.getAllCustomers().size());
    }

    /**
     * TC-CS-05 (Edge): repository is empty.
     * <p>Expect: empty list.
     */
    @Test
    void getAllCustomers_empty() {
        when(customerRepository.findAll()).thenReturn(List.of());

        assertTrue(customerService.getAllCustomers().isEmpty());
    }

    /**
     * TC-CS-06 (Success): 1 customer: Alice, a@x.com.
     * <p>Expect: fullName and email mapped.
     */
    @Test
    void getAllCustomers_mapsFields() {
        when(customerRepository.findAll()).thenReturn(List.of(customer("Alice", "a@x.com")));

        CustomerDTO first = customerService.getAllCustomers().get(0);

        assertEquals("Alice", first.getFullName());
        assertEquals("a@x.com", first.getEmail());
    }

    // getCustomerById
    /**
     * TC-CS-07 (Success): id 1 exists.
     * <p>Expect: fullName "Alice".
     */
    @Test
    void getCustomerById_found() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer("Alice", "a@x.com")));

        assertEquals("Alice", customerService.getCustomerById(1L).getFullName());
    }

    /**
     * TC-CS-08 (Failure): id 99 does not exist.
     * <p>Expect: ResourceNotFoundException "Customer not found".
     */
    @Test
    void getCustomerById_notFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> customerService.getCustomerById(99L));

        assertEquals("Customer not found", ex.getMessage());
    }

    /**
     * TC-CS-09 (Edge): customer linked to user "alice".
     * <p>Expect: username "alice".
     */
    @Test
    void getCustomerById_setsUsernameWhenLinked() {
        Customer c = customer("Alice", "a@x.com");
        User u = new User();
        u.setUsername("alice");
        c.setUser(u);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));

        assertEquals("alice", customerService.getCustomerById(1L).getUsername());
    }

    // updateCustomer
    /**
     * TC-CS-10 (Success): id 1 (Old, old@x.com); DTO: New, new@x.com, 09171234567.
     * <p>Expect: all three fields updated.
     */
    @Test
    void updateCustomer_changesFields() {
        Customer existing = customer("Old", "old@x.com");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(returnsFirstArg());

        CustomerDTO result = customerService.updateCustomer(1L, dto("New", "new@x.com", "09171234567"));

        assertEquals("New", result.getFullName());
        assertEquals("new@x.com", result.getEmail());
        assertEquals("09171234567", result.getPhone());
    }

    /**
     * TC-CS-11 (Failure): id 99 does not exist.
     * <p>Expect: ResourceNotFoundException.
     */
    @Test
    void updateCustomer_notFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerService.updateCustomer(99L, dto("New", "new@x.com", null)));
    }

    /**
     * TC-CS-12 (Edge): id 99 does not exist.
     * <p>Expect: save is never called.
     */
    @Test
    void updateCustomer_notFound_doesNotSave() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerService.updateCustomer(99L, dto("New", "new@x.com", null)));

        verify(customerRepository, never()).save(any(Customer.class));
    }

    // deleteCustomer
    /**
     * TC-CS-13 (Success): id 1 exists.
     * <p>Expect: repository delete called with that customer.
     */
    @Test
    void deleteCustomer_deletes() {
        Customer existing = customer("Alice", "a@x.com");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));

        customerService.deleteCustomer(1L);

        verify(customerRepository).delete(existing);
    }

    /**
     * TC-CS-14 (Failure): id 99 does not exist.
     * <p>Expect: ResourceNotFoundException.
     */
    @Test
    void deleteCustomer_notFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.deleteCustomer(99L));
    }

    /**
     * TC-CS-15 (Edge): id 99 does not exist.
     * <p>Expect: delete is never called.
     */
    @Test
    void deleteCustomer_notFound_doesNotDelete() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.deleteCustomer(99L));

        verify(customerRepository, never()).delete(any(Customer.class));
    }

    // getMyProfile
    /**
     * TC-CS-16 (Success): user "alice" linked to customer Alice.
     * <p>Expect: fullName "Alice", username "alice".
     */
    @Test
    void getMyProfile_found() {
        Customer c = customer("Alice", "a@x.com");
        User u = new User();
        u.setUsername("alice");
        u.setCustomer(c);
        c.setUser(u);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(u));

        CustomerDTO result = customerService.getMyProfile("alice");

        assertEquals("Alice", result.getFullName());
        assertEquals("alice", result.getUsername());
    }

    /**
     * TC-CS-17 (Failure): username "ghost" does not exist.
     * <p>Expect: ResourceNotFoundException "Customer not found".
     */
    @Test
    void getMyProfile_unknownUser() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> customerService.getMyProfile("ghost"));

        assertEquals("Customer not found", ex.getMessage());
    }

    /**
     * TC-CS-18 (Edge): user "orphan" has no customer profile.
     * <p>Expect: ResourceNotFoundException.
     */
    @Test
    void getMyProfile_userWithoutCustomer() {
        User u = new User();
        u.setUsername("orphan");
        when(userRepository.findByUsername("orphan")).thenReturn(Optional.of(u));

        assertThrows(ResourceNotFoundException.class, () -> customerService.getMyProfile("orphan"));
    }
}