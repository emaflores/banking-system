package com.banking.userservice.service;

import com.banking.userservice.dto.AuthResponse;
import com.banking.userservice.dto.LoginRequest;
import com.banking.userservice.dto.RegisterRequest;
import com.banking.userservice.exception.InvalidCredentialsException;
import com.banking.userservice.exception.UserAlreadyExistsException;
import com.banking.userservice.model.Role;
import com.banking.userservice.model.User;
import com.banking.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123");
        registerRequest.setFullName("Test User");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Password123");

        mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded-password")
                .fullName("Test User")
                .role(Role.USER)
                .build();
    }

    @Test
    void should_register_new_user_successfully() {
        // Given
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtService.generateToken(registerRequest.getEmail())).thenReturn("jwt-token");

        // When
        AuthResponse result = authService.register(registerRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getEmail()).isEqualTo(registerRequest.getEmail());
        assertThat(result.getFullName()).isEqualTo(registerRequest.getFullName());

        verify(userRepository).findByEmail(registerRequest.getEmail());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(registerRequest.getEmail());
    }

    @Test
    void should_throw_exception_when_user_already_exists() {
        // Given
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(mockUser));

        // When / Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User already exists");

        verify(userRepository).findByEmail(registerRequest.getEmail());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void should_login_user_successfully() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), mockUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(loginRequest.getEmail())).thenReturn("jwt-token");

        // When
        AuthResponse result = authService.login(loginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getEmail()).isEqualTo(loginRequest.getEmail());
        assertThat(result.getFullName()).isEqualTo(mockUser.getFullName());

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), mockUser.getPassword());
        verify(jwtService).generateToken(loginRequest.getEmail());
    }

    @Test
    void should_throw_exception_when_user_not_found() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid credentials");

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void should_throw_exception_when_password_incorrect() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), mockUser.getPassword())).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid credentials");

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), mockUser.getPassword());
        verify(jwtService, never()).generateToken(any());
    }
}