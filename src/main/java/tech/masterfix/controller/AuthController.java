package tech.masterfix.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.masterfix.dto.ApiResponse;
import tech.masterfix.dto.CustomerProfileResponse;
import tech.masterfix.dto.RegisterRequest;
import tech.masterfix.service.CustomerService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CustomerService customerService;

    public AuthController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        CustomerProfileResponse profile = customerService.register(request);
        return ResponseEntity.ok(ApiResponse.ok(profile, "Registration successful"));
    }
}
