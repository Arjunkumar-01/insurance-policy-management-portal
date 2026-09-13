package com.insurance.portal.security;

import com.insurance.portal.common.dto.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request,
						 HttpServletResponse response,
						 AuthenticationException authException) throws IOException, ServletException {

		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType("application/json");

		ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
				.success(false)
				.status(HttpStatus.UNAUTHORIZED.value())
				.message("Unauthorized access. Please provide valid credentials.")
				.build();

		String body = String.format(
				"{\"success\":%s,\"status\":%d,\"message\":\"%s\",\"timestamp\":\"%s\"}",
				apiResponse.isSuccess(),
				apiResponse.getStatus(),
				apiResponse.getMessage(),
				apiResponse.getTimestamp()
		);

		response.getWriter().write(body);
	}
}
