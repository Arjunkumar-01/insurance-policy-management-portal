package com.insurance.portal.auth.validator;

import com.insurance.portal.auth.dto.request.LoginRequest;
import com.insurance.portal.auth.dto.request.RegisterRequest;
import com.insurance.portal.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import com.insurance.portal.exception.BadRequestException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthValidator {

    private final CustomerRepository customerRepository;

//    Validates registration request
    public void validateRegistration(RegisterRequest request) {
        validatePassword(request);

        validateUsername(request.getUsername());

        validateEmail(request.getEmail());
    }

//    Validate login request
    public void validateLogin(LoginRequest request){

        if(request == null) {
            throw new BadRequestException("Login request cannot be null.");
        }
    }

//    Password and Confirm password should match.
    private void validatePassword(RegisterRequest request) {
        if(!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and Confirm Password do not match.");
        }
    }

//    Username should be unique
    private void validateUsername(String username) {
        if(customerRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already exists.");
        }
    }

//    Email should be unique
    private void validateEmail(String email) {
        if(customerRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists.");
        }
    }
}
