package tech.masterfix.service;

import org.springframework.stereotype.Service;
import tech.masterfix.dto.BookingResponse;

@Service
public class BookingNotificationService {

    private final TelegramNotificationService telegramNotificationService;
    private final WhatsAppNotificationService whatsAppNotificationService;

    public BookingNotificationService(TelegramNotificationService telegramNotificationService,
                                      WhatsAppNotificationService whatsAppNotificationService) {
        this.telegramNotificationService = telegramNotificationService;
        this.whatsAppNotificationService = whatsAppNotificationService;
    }

    public void notifyOwner(BookingResponse booking) {
        booking.setWhatsappUrl(whatsAppNotificationService.buildWaMeUrl(booking));

        if (telegramNotificationService.isConfigured()) {
            if (telegramNotificationService.notifyOwner(booking)) {
                booking.setOwnerNotified(true);
                booking.setNotifyChannel("telegram");
                return;
            }
        }

        if (whatsAppNotificationService.notifyOwner(booking)) {
            booking.setOwnerNotified(true);
            booking.setNotifyChannel("whatsapp");
        }
    }
}
