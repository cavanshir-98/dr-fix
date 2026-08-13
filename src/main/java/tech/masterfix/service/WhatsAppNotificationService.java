package tech.masterfix.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import tech.masterfix.dto.BookingResponse;

@Service
public class WhatsAppNotificationService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppNotificationService.class);

    private final RestClient restClient;
    private final String ownerPhone;
    private final String callMeBotApiKey;

    public WhatsAppNotificationService(
            @Value("${whatsapp.owner.phone:+994508972212}") String ownerPhone,
            @Value("${whatsapp.callmebot.apikey:}") String callMeBotApiKey) {
        this.restClient = RestClient.create();
        this.ownerPhone = ownerPhone.replaceAll("[^0-9]", "");
        this.callMeBotApiKey = callMeBotApiKey == null ? "" : callMeBotApiKey.trim();
    }

    public String buildWaMeUrl(BookingResponse booking) {
        String encoded = URLEncoder.encode(BookingMessageFormatter.format(booking), StandardCharsets.UTF_8)
                .replace("+", "%20");
        return "https://wa.me/" + ownerPhone + "?text=" + encoded;
    }

    public boolean notifyOwner(BookingResponse booking) {
        if (callMeBotApiKey.isBlank()) {
            log.debug("WhatsApp CallMeBot API key not set — owner must receive message via customer wa.me link");
            return false;
        }

        try {
            String uri = UriComponentsBuilder
                    .fromHttpUrl("https://api.callmebot.com/whatsapp.php")
                    .queryParam("phone", ownerPhone)
                    .queryParam("text", BookingMessageFormatter.format(booking))
                    .queryParam("apikey", callMeBotApiKey)
                    .encode()
                    .toUriString();

            restClient.get().uri(uri).retrieve().toBodilessEntity();
            log.info("WhatsApp notification sent to owner for booking {}", booking.getConfirmationCode());
            return true;
        } catch (Exception e) {
            log.warn("Failed to send WhatsApp notification via CallMeBot: {}", e.getMessage());
            return false;
        }
    }

    public boolean isAutoNotifyEnabled() {
        return !callMeBotApiKey.isBlank();
    }
}
