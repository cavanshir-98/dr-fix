package tech.masterfix.service;

import tech.masterfix.dto.BookingResponse;

import java.time.format.DateTimeFormatter;

final class BookingMessageFormatter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private BookingMessageFormatter() {}

    static String format(BookingResponse booking) {
        String address = formatAddress(booking);

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
                blankOr(booking.getFullName(), "Guest"),
                booking.getPhone(),
                formatDate(booking),
                formatTime(booking),
                address,
                booking.getApplianceType(),
                booking.getDescription() == null || booking.getDescription().isBlank()
                        ? "-"
                        : booking.getDescription()
        );
    }

    private static String formatAddress(BookingResponse booking) {
        StringBuilder address = new StringBuilder();
        appendPart(address, booking.getAddress());
        appendPart(address, booking.getCity());
        appendPart(address, booking.getState());
        appendPart(address, booking.getZipCode());
        return address.isEmpty() ? "-" : address.toString();
    }

    private static void appendPart(StringBuilder address, String value) {
        if (value == null || value.isBlank() || "-".equals(value.trim())) {
            return;
        }
        if (!address.isEmpty()) {
            address.append(", ");
        }
        address.append(value.trim());
    }

    private static String blankOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String formatDate(BookingResponse booking) {
        return booking.getPreferredDate() != null
                ? booking.getPreferredDate().format(DATE_FMT)
                : "To be scheduled";
    }

    private static String formatTime(BookingResponse booking) {
        return booking.getPreferredTime() != null
                ? booking.getPreferredTime().format(TIME_FMT)
                : "To be scheduled";
    }
}
