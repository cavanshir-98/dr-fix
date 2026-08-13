package tech.masterfix.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    SecurityFilterChain adminSecurityFilterChain(HttpSecurity http,
                                                 AuthHandlers authHandlers,
                                                 AdminUserDetailsService adminUserDetailsService) throws Exception {
        http
                .securityMatcher(new OrRequestMatcher(
                        new AntPathRequestMatcher("/login.html"),
                        new AntPathRequestMatcher("/login/admin"),
                        new AntPathRequestMatcher("/admin.html"),
                        new AntPathRequestMatcher("/admin/**"),
                        new AntPathRequestMatcher("/api/bookings", "GET")
                ))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login.html", "/login/admin").permitAll()
                        .anyRequest().hasRole("ADMIN")
                )
                .userDetailsService(adminUserDetailsService)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login.html"))
                )
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login/admin")
                        .successHandler(authHandlers)
                        .failureHandler(authHandlers)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/?logout=1")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain appSecurityFilterChain(HttpSecurity http,
                                               AuthHandlers authHandlers,
                                               ApiAuthenticationEntryPoint apiAuthenticationEntryPoint,
                                               CustomerUserDetailsService customerUserDetailsService) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/register.html",
                                "/account/login.html",
                                "/login/customer",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/api/auth/register",
                                "/api/services",
                                "/api/bookings/slots",
                                "/api/contact"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/bookings").hasRole("CUSTOMER")
                        .requestMatchers("/account.html", "/api/account/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .anyRequest().permitAll()
                )
                .userDetailsService(customerUserDetailsService)
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/account/login.html"),
                                new OrRequestMatcher(
                                        new AntPathRequestMatcher("/account.html"),
                                        new AntPathRequestMatcher("/api/account/**")
                                )
                        )
                        .defaultAuthenticationEntryPointFor(
                                apiAuthenticationEntryPoint,
                                new AntPathRequestMatcher("/api/bookings", "POST")
                        )
                )
                .formLogin(form -> form
                        .loginPage("/account/login.html")
                        .loginProcessingUrl("/login/customer")
                        .successHandler(authHandlers)
                        .failureHandler(authHandlers)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/?logout=1")
                        .permitAll()
                );

        return http.build();
    }
}
