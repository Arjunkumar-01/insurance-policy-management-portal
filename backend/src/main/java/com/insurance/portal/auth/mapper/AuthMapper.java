package com.insurance.portal.auth.mapper;

import com.insurance.portal.auth.dto.request.RegisterRequest;
import com.insurance.portal.auth.dto.response.RegisterResponse;
import com.insurance.portal.customer.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

//    Maps RegisterRequest DTO to Customer Entity
//    Business-specific fields like password encoding and role assignment are handled in the service layer

    public Customer toCustomer(RegisterRequest request) {
        return Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .username(request.getUsername())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .build();
    }

//    Maps Customer Entity to RegisterResponse DTO

    public RegisterResponse toRegisterResponse(Customer customer) {
        return RegisterResponse.builder()
                .customerId(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .userName(customer.getUsername())
                .email(customer.getEmail())
                .role(customer.getRole())
                .registeredAt(customer.getCreatedAt())
                .build();
    }
}
