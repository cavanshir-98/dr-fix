package tech.masterfix.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tech.masterfix.dto.BookingResponse;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final String RESEND_FALLBACK_FROM = "DrFix <onboarding@resend.dev>";

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
            @Value("${booking.notification.email.to:drfixappliance@gmail.com}") String recipient,
            @Value("${booking.notification.email.from:drfixappliance@gmail.com}") String fromAddress,
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
        this.resendApiKey = resolveResendApiKey(resendApiKey);
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
        if (resendConfigured) {
            log.info("Booking email: Resend API -> {} (from: {})", recipient, resendFrom);
        } else if (smtpConfigured) {
            log.info("Booking email: Gmail SMTP -> {}", recipient);
        } else if (brevoConfigured) {
            log.info("Booking email: Brevo API -> {}", recipient);
        } else {
            log.error("Booking email DISABLED — RESEND_API_KEY is empty. "
                    + "Local: paste re_xxx into .env.local → RESEND_API_KEY=re_xxx → restart. "
                    + "Render: Environment → RESEND_API_KEY → redeploy.");
        }
    }

    public boolean isEmailConfigured() {
        return resendConfigured || smtpConfigured || brevoConfigured;
    }

    public String getConfiguredProvider() {
        if (resendConfigured) return "resend";
        if (smtpConfigured) return "smtp";
        if (brevoConfigured) return "brevo";
        return "none";
    }

    public String getNotificationRecipient() {
        return recipient;
    }

    public boolean notifyOwner(BookingResponse booking) {
        if (recipient.isBlank()) {
            log.warn("Booking email recipient is not configured");
            return false;
        }

        String subject = "New booking — " + booking.getConfirmationCode();
        String body = BookingMessageFormatter.format(booking);

        if (resendConfigured && sendViaResend(subject, body, booking.getConfirmationCode())) {
            return true;
        }
        if (smtpConfigured && sendViaSmtp(subject, body, booking.getConfirmationCode())) {
            return true;
        }
        if (brevoConfigured && sendViaBrevo(subject, body, booking.getConfirmationCode())) {
            return true;
        }

        if (resendConfigured || smtpConfigured || brevoConfigured) {
            log.error("Booking email NOT sent for {} — all configured providers failed (check logs above)",
                    booking.getConfirmationCode());
        } else {
            log.error("Booking email NOT sent for {} — set RESEND_API_KEY in .env.local or Render Environment",
                    booking.getConfirmationCode());
        }
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
        // onboarding@resend.dev works with API key only (no custom sender verification)
        if (sendResendRequest(RESEND_FALLBACK_FROM, subject, body, confirmationCode)) {
            return true;
        }
        if (!resendFrom.equals(RESEND_FALLBACK_FROM)) {
            log.info("Retrying Resend with custom sender for {}", confirmationCode);
            return sendResendRequest(resendFrom, subject, body, confirmationCode);
        }
        return false;
    }

    private boolean sendResendRequest(String from, String subject, String body, String confirmationCode) {
        try {
            Map<String, Object> payload = Map.of(
                    "from", from,
                    "to", List.of(recipient),
                    "subject", subject,
                    "text", body
            );

            ResponseEntity<String> response = restClient.post()
                    .uri("https://api.resend.com/emails")
                    .header("Authorization", "Bearer " + resendApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toEntity(String.class);

            log.info("Booking email sent via Resend (from: {}) to {} for {} — response: {}",
                    from, recipient, confirmationCode, response.getBody());
            return true;
        } catch (RestClientResponseException e) {
            log.error("Resend failed for {} (from: {}): {} — {}",
                    confirmationCode, from, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("Resend failed for {} (from: {}): {}", confirmationCode, from, e.getMessage());
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

    private static String resolveResendApiKey(String injected) {
        String key = trimToEmpty(injected);
        if (!key.isBlank()) {
            return key;
        }
        key = trimToEmpty(System.getProperty("RESEND_API_KEY"));
        if (!key.isBlank()) {
            return key;
        }
        key = trimToEmpty(System.getProperty("resend.api.key"));
        if (!key.isBlank()) {
            return key;
        }
        return trimToEmpty(System.getenv("RESEND_API_KEY"));
    }
}
