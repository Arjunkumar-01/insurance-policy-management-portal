package com.insurance.portal.auth.controller;

import com.insurance.portal.auth.dto.request.LoginRequest;
import com.insurance.portal.auth.dto.request.RegisterRequest;
import com.insurance.portal.auth.dto.response.LoginResponse;
import com.insurance.portal.auth.dto.response.RegisterResponse;
import com.insurance.portal.auth.service.AuthService;
import com.insurance.portal.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<RegisterResponse>builder()
                        .success(true)
                        .status(HttpStatus.CREATED.value())
                        .message("Registration successful")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.<LoginResponse>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Login successful")
                        .data(response)
                        .build()
        );
    }
}

