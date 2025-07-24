package com.banking.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String TEST_SECRET = "test-secret-key-that-is-at-least-32-characters-long";
    private static final long TEST_EXPIRATION = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", TEST_EXPIRATION);
    }

    @Test
    void should_generate_valid_token() {
        // Given
        String email = "test@example.com";

        // When
        String token = jwtService.generateToken(email);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts separated by dots
    }

    @Test
    void should_extract_email_from_token() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generateToken(email);

        // When
        String extractedEmail = jwtService.extractEmail(token);

        // Then
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    void should_validate_token_correctly() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generateToken(email);

        // When
        boolean isValid = jwtService.isTokenValid(token, email);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void should_invalidate_token_with_wrong_email() {
        // Given
        String email = "test@example.com";
        String wrongEmail = "wrong@example.com";
        String token = jwtService.generateToken(email);

        // When
        boolean isValid = jwtService.isTokenValid(token, wrongEmail);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void should_reject_malformed_token() {
        // Given
        String malformedToken = "invalid.token.here";
        String email = "test@example.com";

        // When / Then
        assertThatThrownBy(() -> jwtService.isTokenValid(malformedToken, email))
                .isInstanceOf(Exception.class);
    }

    @Test
    void should_reject_token_with_different_secret() {
        // Given
        String email = "test@example.com";
        
        // Create JWT service with different secret
        JwtService differentSecretService = new JwtService();
        ReflectionTestUtils.setField(differentSecretService, "secretKey", "different-secret-key-32-chars-long");
        ReflectionTestUtils.setField(differentSecretService, "expirationMs", TEST_EXPIRATION);
        
        String tokenWithDifferentSecret = differentSecretService.generateToken(email);

        // When / Then
        assertThatThrownBy(() -> jwtService.isTokenValid(tokenWithDifferentSecret, email))
                .isInstanceOf(Exception.class);
    }

    @Test
    void should_throw_exception_for_short_secret() {
        // Given
        JwtService serviceWithShortSecret = new JwtService();
        ReflectionTestUtils.setField(serviceWithShortSecret, "secretKey", "short");
        ReflectionTestUtils.setField(serviceWithShortSecret, "expirationMs", TEST_EXPIRATION);

        // When / Then
        assertThatThrownBy(() -> serviceWithShortSecret.validateConfig())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT secret must be at least 32 characters");
    }

    @Test
    void should_throw_exception_for_null_secret() {
        // Given
        JwtService serviceWithNullSecret = new JwtService();
        ReflectionTestUtils.setField(serviceWithNullSecret, "secretKey", null);
        ReflectionTestUtils.setField(serviceWithNullSecret, "expirationMs", TEST_EXPIRATION);

        // When / Then
        assertThatThrownBy(() -> serviceWithNullSecret.validateConfig())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT secret must be at least 32 characters and not null");
    }
}