package tech.masterfix.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tech.masterfix.dto.ApiResponse;
import tech.masterfix.dto.BookingResponse;
import tech.masterfix.dto.CustomerProfileResponse;
import tech.masterfix.model.Customer;
import tech.masterfix.service.BookingService;
import tech.masterfix.service.CustomerService;

import java.util.List;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final CustomerService customerService;
    private final BookingService bookingService;

    public AccountController(CustomerService customerService, BookingService bookingService) {
        this.customerService = customerService;
        this.bookingService = bookingService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> getProfile(Authentication authentication) {
        CustomerProfileResponse profile = customerService.getProfile(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(profile, "Profile retrieved"));
    }

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(Authentication authentication) {
        Customer customer = customerService.getByEmail(authentication.getName());
        List<BookingResponse> bookings = bookingService.getCustomerBookings(customer.getId());
        return ResponseEntity.ok(ApiResponse.ok(bookings, "Bookings retrieved"));
    }
}
