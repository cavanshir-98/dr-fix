package tech.masterfix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.masterfix.model.Booking;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByPreferredDate(LocalDate date);
    Optional<Booking> findByIdAndEmail(Long id, String email);
    List<Booking> findAllByOrderByCreatedAtDesc();
    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
