package tech.masterfix.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.masterfix.dto.BookingRequest;
import tech.masterfix.dto.BookingResponse;
import tech.masterfix.model.Booking;
import tech.masterfix.model.BookingStatus;
import tech.masterfix.model.Customer;
import tech.masterfix.repository.BookingRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final LocalTime OPEN_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(20, 0);

    private final BookingRepository bookingRepository;
    private final BookingNotificationService bookingNotificationService;

    public BookingService(BookingRepository bookingRepository,
                          BookingNotificationService bookingNotificationService) {
        this.bookingRepository = bookingRepository;
        this.bookingNotificationService = bookingNotificationService;
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request, Customer customer) {
        validateTimeSlot(request.getPreferredTime());

        Booking booking = new Booking();
        booking.setFullName(customer != null ? customer.getFullName() : request.getFullName());
        booking.setPhone(customer != null ? customer.getPhone() : request.getPhone());
        booking.setEmail(customer != null ? customer.getEmail() : request.getEmail());
        booking.setAddress(request.getAddress());
        booking.setCity(request.getCity());
        booking.setState(request.getState());
        booking.setZipCode(request.getZipCode());
        booking.setApplianceType(request.getApplianceType());
        booking.setDescription(request.getDescription());
        booking.setPreferredDate(request.getPreferredDate());
        booking.setPreferredTime(request.getPreferredTime());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCustomer(customer);

        Booking saved = bookingRepository.save(booking);
        BookingResponse response = toResponse(saved);
        bookingNotificationService.notifyOwner(response);
        return response;
    }

    public BookingResponse getBooking(Long id, String email) {
        Booking booking = bookingRepository.findByIdAndEmail(id, email)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        return toResponse(booking);
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<BookingResponse> getCustomerBookings(Long customerId) {
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<String> getAvailableTimeSlots(LocalDate date) {
        List<LocalTime> booked = bookingRepository.findByPreferredDate(date).stream()
                .map(Booking::getPreferredTime)
                .toList();

        return generateTimeSlots().stream()
                .filter(slot -> !booked.contains(slot))
                .map(LocalTime::toString)
                .collect(Collectors.toList());
    }

    private void validateTimeSlot(LocalTime time) {
        if (time.isBefore(OPEN_TIME) || time.isAfter(CLOSE_TIME.minusHours(1))) {
            throw new IllegalArgumentException("Time must be between 08:00 and 19:00");
        }
        if (time.getMinute() % 30 != 0) {
            throw new IllegalArgumentException("Time slots are available every 30 minutes");
        }
    }

    private List<LocalTime> generateTimeSlots() {
        List<LocalTime> slots = new java.util.ArrayList<>();
        LocalTime current = OPEN_TIME;
        while (!current.isAfter(CLOSE_TIME.minusHours(1))) {
            slots.add(current);
            current = current.plusMinutes(30);
        }
        return slots;
    }

    private BookingResponse toResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setFullName(booking.getFullName());
        response.setPhone(booking.getPhone());
        response.setEmail(booking.getEmail());
        response.setAddress(booking.getAddress());
        response.setCity(booking.getCity());
        response.setState(booking.getState());
        response.setZipCode(booking.getZipCode());
        response.setApplianceType(booking.getApplianceType());
        response.setDescription(booking.getDescription());
        response.setPreferredDate(booking.getPreferredDate());
        response.setPreferredTime(booking.getPreferredTime());
        response.setStatus(booking.getStatus());
        response.setCreatedAt(booking.getCreatedAt());
        response.setConfirmationCode(String.format("DF-%06d", booking.getId()));
        return response;
    }
}
