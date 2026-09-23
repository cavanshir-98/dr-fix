package tech.masterfix.service;

import org.springframework.stereotype.Service;
import tech.masterfix.dto.BookingResponse;

@Service
public class BookingNotificationService {

    private final TelegramNotificationService telegramNotificationService;
    private final WhatsAppNotificationService whatsAppNotificationService;
    private final EmailNotificationService emailNotificationService;

    public BookingNotificationService(TelegramNotificationService telegramNotificationService,
                                      WhatsAppNotificationService whatsAppNotificationService,
                                      EmailNotificationService emailNotificationService) {
        this.telegramNotificationService = telegramNotificationService;
        this.whatsAppNotificationService = whatsAppNotificationService;
        this.emailNotificationService = emailNotificationService;
    }

    public void notifyOwner(BookingResponse booking) {
        booking.setWhatsappUrl(whatsAppNotificationService.buildWaMeUrl(booking));
        booking.setEmailSent(emailNotificationService.notifyOwner(booking));
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
