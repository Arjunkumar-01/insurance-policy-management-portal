package com.insurance.portal.auth.dto.response;

import com.insurance.portal.common.enums.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterResponse {

    private Long customerId;

    private String firstName;

    private String lastName;

    private String userName;

    private String email;

    private Role role;

    private LocalDateTime registeredAt;

    private String message;
}
