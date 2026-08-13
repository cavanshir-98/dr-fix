package tech.masterfix.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.masterfix.config.AdminUserDetailsService;
import tech.masterfix.dto.CustomerProfileResponse;
import tech.masterfix.dto.RegisterRequest;
import tech.masterfix.model.Customer;
import tech.masterfix.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminUserDetailsService adminUserDetailsService;

    public CustomerService(CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder,
                           AdminUserDetailsService adminUserDetailsService) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUserDetailsService = adminUserDetailsService;
    }

    @Transactional
    public CustomerProfileResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        String email = request.getEmail().trim().toLowerCase();

        if (adminUserDetailsService.isAdminEmail(email)) {
            throw new IllegalArgumentException("This email is reserved. Please use a different email or sign in as admin.");
        }

        if (customerRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("This email is already registered. Please sign in instead.");
        }

        Customer customer = new Customer();
        customer.setFullName(request.getFullName().trim());
        customer.setEmail(email);
        customer.setPhone(request.getPhone().trim());
        customer.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        try {
            Customer saved = customerRepository.save(customer);
            return toProfile(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("This email is already registered. Please sign in instead.");
        }
    }

    public Customer getByEmail(String email) {
        return customerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    }

    public CustomerProfileResponse getProfile(String email) {
        return toProfile(getByEmail(email));
    }

    private CustomerProfileResponse toProfile(Customer customer) {
        CustomerProfileResponse profile = new CustomerProfileResponse();
        profile.setId(customer.getId());
        profile.setFullName(customer.getFullName());
        profile.setEmail(customer.getEmail());
        profile.setPhone(customer.getPhone());
        return profile;
    }
}
