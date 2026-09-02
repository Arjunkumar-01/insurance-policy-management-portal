package com.insurance.portal.auth.dto.response;

import com.insurance.portal.common.enums.Role;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {

    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private String username;

    private Role role;

    private String fullName;
}
