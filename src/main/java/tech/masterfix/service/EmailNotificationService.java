package tech.masterfix.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tech.masterfix.dto.BookingResponse;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final RestClient restClient;
    private final JavaMailSender mailSender;
    private final String recipient;
    private final String fromAddress;
    private final String fromName;
    private final String mailPassword;
    private final String brevoApiKey;
    private final String resendApiKey;
    private final String resendFrom;
    private final boolean smtpConfigured;
    private final boolean brevoConfigured;
    private final boolean resendConfigured;

    public EmailNotificationService(
            @Autowired(required = false) JavaMailSender mailSender,
            @Value("${booking.notification.email.to:cavansir.asada@gmail.com}") String recipient,
            @Value("${booking.notification.email.from:Drfixrepairappliancerepair@gmail.com}") String fromAddress,
            @Value("${booking.notification.email.from-name:DrFix}") String fromName,
            @Value("${spring.mail.host:}") String mailHost,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.password:}") String mailPassword,
            @Value("${brevo.api.key:}") String brevoApiKey,
            @Value("${resend.api.key:}") String resendApiKey,
            @Value("${resend.from:DrFix <onboarding@resend.dev>}") String resendFrom) {
        this.restClient = RestClient.create();
        this.mailSender = mailSender;
        this.recipient = trimToEmpty(recipient);
        this.fromAddress = trimToEmpty(fromAddress.isBlank() ? mailUsername : fromAddress);
        this.fromName = trimToEmpty(fromName);
        this.mailPassword = trimToEmpty(mailPassword);
        this.brevoApiKey = trimToEmpty(brevoApiKey);
        this.resendApiKey = trimToEmpty(resendApiKey);
        this.resendFrom = trimToEmpty(resendFrom);
        this.smtpConfigured = mailSender != null
                && !trimToEmpty(mailHost).isBlank()
                && !this.fromAddress.isBlank()
                && !this.mailPassword.isBlank()
                && !this.recipient.isBlank();
        this.brevoConfigured = !this.brevoApiKey.isBlank() && !this.recipient.isBlank() && !this.fromAddress.isBlank();
        this.resendConfigured = !this.resendApiKey.isBlank() && !this.recipient.isBlank();
    }

    @PostConstruct
    void logConfiguration() {
        if (smtpConfigured) {
            log.info("Booking email: Gmail SMTP -> {}", recipient);
        } else if (resendConfigured) {
            log.info("Booking email: Resend API -> {}", recipient);
        } else if (brevoConfigured) {
            log.info("Booking email: Brevo API -> {}", recipient);
        } else {
            log.warn("Booking email NOT configured. Set SPRING_MAIL_PASSWORD or RESEND_API_KEY on Render.");
        }
    }

    public boolean notifyOwner(BookingResponse booking) {
        if (recipient.isBlank()) {
            log.warn("Booking email recipient is not configured");
            return false;
        }

        String subject = "New booking — " + booking.getConfirmationCode();
        String body = BookingMessageFormatter.format(booking);

        if (smtpConfigured && sendViaSmtp(subject, body, booking.getConfirmationCode())) {
            return true;
        }
        if (resendConfigured && sendViaResend(subject, body, booking.getConfirmationCode())) {
            return true;
        }
        if (brevoConfigured && sendViaBrevo(subject, body, booking.getConfirmationCode())) {
            return true;
        }

        log.warn("Booking email NOT sent for {} — configure SPRING_MAIL_PASSWORD or RESEND_API_KEY on Render", booking.getConfirmationCode());
        return false;
    }

    private boolean sendViaSmtp(String subject, String body, String confirmationCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipient);
            message.setFrom(fromAddress);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Booking email sent via SMTP to {} for {}", recipient, confirmationCode);
            return true;
        } catch (Exception e) {
            log.warn("SMTP booking email failed for {}: {}", confirmationCode, e.getMessage());
            return false;
        }
    }

    private boolean sendViaResend(String subject, String body, String confirmationCode) {
        try {
            Map<String, Object> payload = Map.of(
                    "from", resendFrom,
                    "to", List.of(recipient),
                    "subject", subject,
                    "text", body
            );

            restClient.post()
                    .uri("https://api.resend.com/emails")
                    .header("Authorization", "Bearer " + resendApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Booking email sent via Resend to {} for {}", recipient, confirmationCode);
            return true;
        } catch (Exception e) {
            log.warn("Resend booking email failed for {}: {}", confirmationCode, e.getMessage());
            return false;
        }
    }

    private boolean sendViaBrevo(String subject, String body, String confirmationCode) {
        try {
            Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", fromName, "email", fromAddress),
                    "to", List.of(Map.of("email", recipient)),
                    "subject", subject,
                    "textContent", body
            );

            restClient.post()
                    .uri("https://api.brevo.com/v3/smtp/email")
                    .header("api-key", brevoApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Booking email sent via Brevo to {} for {}", recipient, confirmationCode);
            return true;
        } catch (Exception e) {
            log.warn("Brevo booking email failed for {}: {}", confirmationCode, e.getMessage());
            return false;
        }
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
