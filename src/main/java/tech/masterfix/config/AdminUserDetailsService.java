package tech.masterfix.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service("adminUserDetailsService")
public class AdminUserDetailsService implements UserDetailsService {

    private final String adminUsername;
    private final String adminPasswordHash;

    public AdminUserDetailsService(PasswordEncoder passwordEncoder,
                                   @Value("${admin.username}") String adminUsername,
                                   @Value("${admin.password}") String adminPassword) {
        this.adminUsername = adminUsername.trim();
        this.adminPasswordHash = passwordEncoder.encode(adminPassword);
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public boolean isAdminEmail(String email) {
        return adminUsername.equalsIgnoreCase(email.trim());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!adminUsername.equalsIgnoreCase(username.trim())) {
            throw new UsernameNotFoundException("Admin not found");
        }

        return User.withUsername(adminUsername)
                .password(adminPasswordHash)
                .roles("ADMIN")
                .build();
    }
}
