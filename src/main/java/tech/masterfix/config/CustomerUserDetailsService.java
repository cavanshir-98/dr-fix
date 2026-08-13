package tech.masterfix.config;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tech.masterfix.model.Customer;
import tech.masterfix.repository.CustomerRepository;

@Service("customerUserDetailsService")
public class CustomerUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private final AdminUserDetailsService adminUserDetailsService;

    public CustomerUserDetailsService(CustomerRepository customerRepository,
                                      AdminUserDetailsService adminUserDetailsService) {
        this.customerRepository = customerRepository;
        this.adminUserDetailsService = adminUserDetailsService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String email = username.trim().toLowerCase();

        if (adminUserDetailsService.isAdminEmail(email)) {
            throw new UsernameNotFoundException("Use admin login for this account");
        }

        Customer customer = customerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found"));

        return User.withUsername(customer.getEmail())
                .password(customer.getPasswordHash())
                .roles("CUSTOMER")
                .build();
    }
}
