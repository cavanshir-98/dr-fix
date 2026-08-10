package tech.masterfix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.masterfix.model.ContactMessage;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
}
