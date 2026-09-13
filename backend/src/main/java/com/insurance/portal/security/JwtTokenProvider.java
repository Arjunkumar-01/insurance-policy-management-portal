package com.insurance.portal.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

	@Value("${security.jwt.secret}")
	private String jwtSecret;

	@Value("${security.jwt.expiration-ms:3600000}")
	private long jwtExpirationMs;

	private SecretKey secretKey;

	@PostConstruct
	public void init() {
		if (jwtSecret == null || jwtSecret.isBlank() || jwtSecret.length() < 32) {
			throw new IllegalStateException("JWT secret must be configured and at least 32 characters long.");
		}
		secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(String username, String role) {
		Instant now = Instant.now();

		return Jwts.builder()
				.subject(username)
				.claim("role", role)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusMillis(jwtExpirationMs)))
				.signWith(secretKey)
				.compact();
	}

	public String getUsernameFromToken(String token) {
		return getClaims(token).getSubject();
	}

	public boolean validateToken(String token) {
		try {
			getClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException ex) {
			return false;
		}
	}

	private Claims getClaims(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
