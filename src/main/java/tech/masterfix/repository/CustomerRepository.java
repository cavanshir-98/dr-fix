package tech.masterfix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.masterfix.model.Customer;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
