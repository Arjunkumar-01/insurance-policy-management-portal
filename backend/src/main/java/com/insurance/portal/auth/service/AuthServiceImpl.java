package com.insurance.portal.auth.service;

import com.insurance.portal.auth.dto.request.LoginRequest;
import com.insurance.portal.auth.dto.request.RegisterRequest;
import com.insurance.portal.auth.dto.response.LoginResponse;
import com.insurance.portal.auth.dto.response.RegisterResponse;
import com.insurance.portal.auth.mapper.AuthMapper;
import com.insurance.portal.auth.validator.AuthValidator;
import com.insurance.portal.common.enums.Role;
import com.insurance.portal.customer.entity.Customer;
import com.insurance.portal.customer.repository.CustomerRepository;
import com.insurance.portal.exception.DuplicateResourceException;
import com.insurance.portal.exception.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService{

    private final CustomerRepository customerRepository;

    private final AuthMapper authMapper;

    private final PasswordEncoder passwordEncoder;

    private final AuthValidator authValidator;

    @Override
    public RegisterResponse register(RegisterRequest request) {

        log.info("Registration request received for username: {}", request.getUsername());

        authValidator.validateRegistration(request);

        Customer customer = authMapper.toCustomer(request);

        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setRole(Role.CUSTOMER);
        customer.setEnabled(true);

        Customer savedCustomer = customerRepository.save(customer);

        log.info("Customer registered successfully with ID: {}", savedCustomer.getId());

        RegisterResponse response = authMapper.toRegisterResponse(savedCustomer);

        response.setMessage("Customer registered successfully");

        return response;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {

        log.info("Login attempt for username: {}", request.getUsername());

        authValidator.validateLogin(request);

        Customer customer = customerRepository.findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new UnauthorizedException(("Invalid username or password.")));

        if(!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {

            throw new UnauthorizedException("Invalid username or password.");
        }

        log.info("Login successful for username: {}", customer.getUsername());

        return LoginResponse.builder()
                .username(customer.getUsername())
                .role(customer.getRole())
                .accessToken(null)
                .tokenType("Bearer")
                .build();
    }
}
