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

@Service
public class TelegramNotificationService {

    private static final Logger log = LoggerFactory.getLogger(TelegramNotificationService.class);

    private final RestClient restClient;
    private final String botToken;
    private final String chatId;

    public TelegramNotificationService(
            @Value("${telegram.bot.token:}") String botToken,
            @Value("${telegram.chat.id:}") String chatId) {
        this.restClient = RestClient.create();
        this.botToken = botToken == null ? "" : botToken.trim();
        this.chatId = chatId == null ? "" : chatId.trim();
    }

    public boolean isConfigured() {
        return !botToken.isBlank() && !chatId.isBlank();
    }

    public boolean notifyOwner(BookingResponse booking) {
        if (!isConfigured()) {
            return false;
        }

        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("chat_id", chatId);
            form.add("text", BookingMessageFormatter.format(booking));

            restClient.post()
                    .uri("https://api.telegram.org/bot" + botToken + "/sendMessage")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Telegram notification sent for booking {}", booking.getConfirmationCode());
            return true;
        } catch (Exception e) {
            log.warn("Failed to send Telegram notification: {}", e.getMessage());
            return false;
        }
    }
}
