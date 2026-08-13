package tech.masterfix.service;

import tech.masterfix.dto.BookingResponse;

import java.time.format.DateTimeFormatter;

final class BookingMessageFormatter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private BookingMessageFormatter() {}

    static String format(BookingResponse booking) {
        String address = String.join(", ",
                booking.getAddress(),
                booking.getCity(),
                booking.getState(),
                booking.getZipCode());

        return String.format(
                "New booking!%n%n" +
                "Code: %s%n" +
                "Name: %s%n" +
                "Phone: %s%n" +
                "Date: %s%n" +
                "Time: %s%n" +
                "Address: %s%n" +
                "Service: %s%n" +
                "Notes: %s",
                booking.getConfirmationCode(),
                booking.getFullName(),
                booking.getPhone(),
                booking.getPreferredDate().format(DATE_FMT),
                booking.getPreferredTime().format(TIME_FMT),
                address,
                booking.getApplianceType(),
                booking.getDescription() == null || booking.getDescription().isBlank()
                        ? "-"
                        : booking.getDescription()
        );
    }
}
