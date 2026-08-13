package tech.masterfix.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import tech.masterfix.dto.*;
import tech.masterfix.model.ApplianceService;
import tech.masterfix.model.Customer;
import tech.masterfix.repository.ApplianceServiceRepository;
import tech.masterfix.service.BookingService;
import tech.masterfix.service.ContactService;
import tech.masterfix.service.CustomerService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    private final BookingService bookingService;
    private final ContactService contactService;
    private final CustomerService customerService;
    private final ApplianceServiceRepository applianceServiceRepository;

    public ApiController(BookingService bookingService,
                         ContactService contactService,
                         CustomerService customerService,
                         ApplianceServiceRepository applianceServiceRepository) {
        this.bookingService = bookingService;
        this.contactService = contactService;
        this.customerService = customerService;
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

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookings() {
        List<BookingResponse> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(ApiResponse.ok(bookings, "Bookings retrieved"));
    }

    @PostMapping("/bookings")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        Customer customer = resolveCustomer(authentication);
        if (customer == null) {
            throw new IllegalArgumentException("Please register and sign in to make a booking");
        }
        BookingResponse booking = bookingService.createBooking(request, customer);
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

    private Customer resolveCustomer(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        boolean isCustomer = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        if (!isCustomer) {
            return null;
        }

        return customerService.getByEmail(authentication.getName());
    }
}
