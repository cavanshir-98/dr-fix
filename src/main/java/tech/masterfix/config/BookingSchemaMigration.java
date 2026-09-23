package tech.masterfix.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class BookingSchemaMigration {

    private static final Logger log = LoggerFactory.getLogger(BookingSchemaMigration.class);

    /** Legacy schema had NOT NULL on fields that are optional for guest bookings. */
    private static final String[] GUEST_OPTIONAL_COLUMNS = {
            "full_name",
            "email",
            "address",
            "city",
            "state",
            "zip_code",
            "description",
            "preferred_date",
            "preferred_time",
            "customer_id"
    };

    private final JdbcTemplate jdbcTemplate;

    public BookingSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void relaxGuestBookingColumns() {
        for (String column : GUEST_OPTIONAL_COLUMNS) {
            relaxNotNull("bookings", column);
        }
    }

    private void relaxNotNull(String table, String column) {
        try {
            jdbcTemplate.execute("ALTER TABLE " + table + " ALTER COLUMN " + column + " DROP NOT NULL");
            log.info("Relaxed NOT NULL on {}.{}", table, column);
        } catch (Exception e) {
            log.debug("{}.{} already nullable or not applicable: {}", table, column, e.getMessage());
        }
    }
}
