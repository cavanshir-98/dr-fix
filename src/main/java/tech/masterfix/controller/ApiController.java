package tech.masterfix.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.masterfix.dto.*;
import tech.masterfix.model.ApplianceService;
import tech.masterfix.repository.ApplianceServiceRepository;
import tech.masterfix.service.BookingService;
import tech.masterfix.service.ContactService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    private final BookingService bookingService;
    private final ContactService contactService;
    private final ApplianceServiceRepository applianceServiceRepository;

    public ApiController(BookingService bookingService,
                         ContactService contactService,
                         ApplianceServiceRepository applianceServiceRepository) {
        this.bookingService = bookingService;
        this.contactService = contactService;
        this.applianceServiceRepository = applianceServiceRepository;
    }

    @GetMapping("/services")
    public ResponseEntity<ApiResponse<List<ApplianceService>>> getServices(
            @RequestParam(required = false) String category) {
        List<ApplianceService> services = category != null
                ? applianceServiceRepository.findByCategory(category)
                : applianceServiceRepository.findAll();
        return ResponseEntity.ok(ApiResponse.ok(services, "Services retrieved"));
    }

    @GetMapping("/bookings/slots")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<String> slots = bookingService.getAvailableTimeSlots(date);
        return ResponseEntity.ok(ApiResponse.ok(slots, "Available slots retrieved"));
    }

    @PostMapping("/bookings")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request) {
        BookingResponse booking = bookingService.createBooking(request);
        return ResponseEntity.ok(ApiResponse.ok(booking, "Booking confirmed successfully"));
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
            @PathVariable Long id,
            @RequestParam String email) {
        BookingResponse booking = bookingService.getBooking(id, email);
        return ResponseEntity.ok(ApiResponse.ok(booking, "Booking retrieved"));
    }

    @PostMapping("/contact")
    public ResponseEntity<ApiResponse<Object>> submitContact(
            @Valid @RequestBody ContactRequest request) {
        contactService.saveMessage(request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Message sent successfully"));
    }
}
