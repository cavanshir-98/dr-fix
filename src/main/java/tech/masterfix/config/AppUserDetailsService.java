package tech.masterfix.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.masterfix.model.Customer;
import tech.masterfix.repository.CustomerRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPasswordHash;

    public AppUserDetailsService(CustomerRepository customerRepository,
                                 PasswordEncoder passwordEncoder,
                                 @Value("${admin.username}") String adminUsername,
                                 @Value("${admin.password}") String adminPassword) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPasswordHash = passwordEncoder.encode(adminPassword);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (adminUsername.equals(username)) {
            return User.withUsername(adminUsername)
                    .password(adminPasswordHash)
                    .roles("ADMIN")
                    .build();
        }

        Customer customer = customerRepository.findByEmailIgnoreCase(username.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.withUsername(customer.getEmail())
                .password(customer.getPasswordHash())
                .roles("CUSTOMER")
                .build();
    }
}
