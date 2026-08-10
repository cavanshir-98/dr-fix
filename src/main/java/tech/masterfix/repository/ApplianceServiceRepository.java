package tech.masterfix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.masterfix.model.ApplianceService;
import java.util.List;

public interface ApplianceServiceRepository extends JpaRepository<ApplianceService, Long> {
    List<ApplianceService> findByCategory(String category);
}
