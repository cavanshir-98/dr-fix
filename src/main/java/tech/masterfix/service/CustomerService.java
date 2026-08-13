package tech.masterfix.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.masterfix.dto.CustomerProfileResponse;
import tech.masterfix.dto.RegisterRequest;
import tech.masterfix.model.Customer;
import tech.masterfix.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CustomerProfileResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (customerRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        Customer customer = new Customer();
        customer.setFullName(request.getFullName().trim());
        customer.setEmail(email);
        customer.setPhone(request.getPhone().trim());
        customer.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        Customer saved = customerRepository.save(customer);
        return toProfile(saved);
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
