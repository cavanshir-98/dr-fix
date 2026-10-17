package tech.masterfix.service;

import org.springframework.stereotype.Service;
import tech.masterfix.dto.BookingResponse;

@Service
public class BookingNotificationService {

    private final TelegramNotificationService telegramNotificationService;
    private final EmailNotificationService emailNotificationService;
    private final SmsNotificationService smsNotificationService;

    public BookingNotificationService(TelegramNotificationService telegramNotificationService,
                                      EmailNotificationService emailNotificationService,
                                      SmsNotificationService smsNotificationService) {
        this.telegramNotificationService = telegramNotificationService;
        this.emailNotificationService = emailNotificationService;
        this.smsNotificationService = smsNotificationService;
    }

    public void notifyOwner(BookingResponse booking) {
        booking.setEmailSent(emailNotificationService.notifyOwner(booking));
        if (telegramNotificationService.isConfigured() && telegramNotificationService.notifyOwner(booking)) {
            booking.setOwnerNotified(true);
            booking.setNotifyChannel("telegram");
        }
        booking.setSmsSent(smsNotificationService.notifyCustomer(booking));
    }
}
