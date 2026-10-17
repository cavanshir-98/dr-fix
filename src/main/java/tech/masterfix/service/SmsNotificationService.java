package tech.masterfix.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import tech.masterfix.dto.BookingResponse;

import java.time.format.DateTimeFormatter;

@Service
public class SmsNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationService.class);
    private static final DateTimeFormatter SMS_DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter SMS_TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    private final RestClient restClient;
    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private final String publicUrl;

    public SmsNotificationService(
            @Value("${twilio.account.sid:}") String accountSid,
            @Value("${twilio.auth.token:}") String authToken,
            @Value("${twilio.from.number:}") String fromNumber,
            @Value("${app.public.url:https://dr-fix.onrender.com}") String publicUrl) {
        this.restClient = RestClient.create();
        this.accountSid = accountSid == null ? "" : accountSid.trim();
        this.authToken = authToken == null ? "" : authToken.trim();
        this.fromNumber = fromNumber == null ? "" : fromNumber.trim();
        this.publicUrl = publicUrl == null || publicUrl.isBlank()
                ? "https://dr-fix.onrender.com"
                : publicUrl.trim().replaceAll("/$", "");
    }

    public boolean isConfigured() {
        return !accountSid.isBlank() && !authToken.isBlank() && !fromNumber.isBlank();
    }

    public boolean notifyCustomer(BookingResponse booking) {
        if (!isConfigured()) {
            return false;
        }

        String to = toE164(booking.getPhone());
        if (to == null) {
            log.warn("Skipping SMS for {} — invalid phone number", booking.getConfirmationCode());
            return false;
        }

        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("To", to);
            form.add("From", fromNumber);
            form.add("Body", buildCustomerMessage(booking));

            restClient.post()
                    .uri("https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json")
                    .headers(headers -> headers.setBasicAuth(accountSid, authToken))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();

            log.info("SMS confirmation sent to {} for booking {}", maskPhone(to), booking.getConfirmationCode());
            return true;
        } catch (Exception e) {
            log.warn("Failed to send SMS for {}: {}", booking.getConfirmationCode(), e.getMessage());
            return false;
        }
    }

    private String buildCustomerMessage(BookingResponse booking) {
        String date = booking.getPreferredDate() != null
                ? booking.getPreferredDate().format(SMS_DATE_FMT)
                : "TBD";
        String time = booking.getPreferredTime() != null
                ? booking.getPreferredTime().format(SMS_TIME_FMT)
                : "TBD";

        return String.format(
                "DrFix: Booking confirmed! Code: %s. %s at %s. Track your booking: %s/track-booking.html",
                booking.getConfirmationCode(),
                date,
                time,
                publicUrl
        );
    }

    static String toE164(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 10) {
            return "+1" + digits;
        }
        if (digits.length() == 11 && digits.startsWith("1")) {
            return "+" + digits;
        }
        if (digits.length() >= 11) {
            return "+" + digits;
        }

        return null;
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        return "****" + phone.substring(phone.length() - 4);
    }
}
