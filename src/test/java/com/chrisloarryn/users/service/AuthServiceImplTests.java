package com.chrisloarryn.users.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import com.chrisloarryn.users.config.JwtProperties;
import com.chrisloarryn.users.domain.Phone;
import com.chrisloarryn.users.domain.User;
import com.chrisloarryn.users.error.ConflictException;
import com.chrisloarryn.users.error.InvalidCredentialsException;
import com.chrisloarryn.users.repository.UserRepository;
import com.chrisloarryn.users.security.JwtService;
import com.chrisloarryn.users.service.impl.AuthServiceImpl;
import com.chrisloarryn.users.service.impl.UserMapperImpl;
import com.chrisloarryn.users.web.dto.request.LoginRequest;
import com.chrisloarryn.users.web.dto.request.PhoneRequest;
import com.chrisloarryn.users.web.dto.request.RegisterUserRequest;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.spec.SecretKeySpec;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceImplTests {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private PasswordPolicyValidator passwordPolicyValidator;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        passwordPolicyValidator = mock(PasswordPolicyValidator.class);
        authService = new AuthServiceImpl(
                userRepository,
                passwordEncoder,
                passwordPolicyValidator,
                new UserMapperImpl(),
                jwtService());
    }

    @Test
    void registerNormalizesEmailAndTrimsNameBeforeSaving() {
        RegisterUserRequest request = new RegisterUserRequest(
                "  Jane Doe  ",
                "  Jane.Doe@Example.COM  ",
                "StrongPass1!",
                List.of(new PhoneRequest("123456789", "1", "56")));
        when(passwordPolicyValidator.isValid("StrongPass1!")).thenReturn(true);
        when(userRepository.existsByEmailIgnoreCase("jane.doe@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(user, "createdAt", Instant.parse("2026-03-08T00:00:00Z"));
            ReflectionTestUtils.setField(user, "updatedAt", Instant.parse("2026-03-08T00:00:00Z"));
            for (Phone phone : user.getPhones()) {
                ReflectionTestUtils.setField(phone, "createdAt", Instant.parse("2026-03-08T00:00:00Z"));
            }
            return user;
        });

        var response = authService.register(request);

        assertEquals("jane.doe@example.com", response.user().email());
        assertEquals("Jane Doe", response.user().name());
        assertNotNull(response.user().lastLoginAt());
        verify(userRepository).save(argThat(savedUser ->
                !"StrongPass1!".equals(savedUser.getPasswordHash())
                        && passwordEncoder.matches("StrongPass1!", savedUser.getPasswordHash())));
        assertDoesNotThrow(() -> verify(userRepository).existsByEmailIgnoreCase("jane.doe@example.com"));
    }

    @Test
    void registerRejectsDuplicateEmailsAfterNormalization() {
        RegisterUserRequest request = new RegisterUserRequest(
                "Jane Doe",
                "  DUPLICATE@Example.COM ",
                "StrongPass1!",
                List.of(new PhoneRequest("123456789", "1", "56")));
        when(userRepository.existsByEmailIgnoreCase("duplicate@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userRepository).existsByEmailIgnoreCase("duplicate@example.com");
    }

    @Test
    void loginRejectsInactiveUsers() {
        User inactiveUser = new User("Jane Doe", "inactive@example.com", passwordEncoder.encode("StrongPass1!"));
        inactiveUser.deactivate();
        when(userRepository.findByEmailIgnoreCase("inactive@example.com")).thenReturn(Optional.of(inactiveUser));

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest(" inactive@example.com ", "StrongPass1!")));
    }

    @Test
    void loginNormalizesEmailAndUpdatesLastLoginAt() {
        User user = new User("Jane Doe", "active@example.com", passwordEncoder.encode("StrongPass1!"));
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(user, "createdAt", Instant.parse("2026-03-01T00:00:00Z"));
        ReflectionTestUtils.setField(user, "updatedAt", Instant.parse("2026-03-01T00:00:00Z"));
        user.replacePhones(List.of(new Phone("123456789", "1", "56")));
        for (Phone phone : user.getPhones()) {
            ReflectionTestUtils.setField(phone, "createdAt", Instant.parse("2026-03-01T00:00:00Z"));
        }

        when(userRepository.findByEmailIgnoreCase("active@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Instant beforeLogin = Instant.now().minusSeconds(1);
        var response = authService.login(new LoginRequest(" Active@Example.com ", "StrongPass1!"));

        verify(userRepository).findByEmailIgnoreCase("active@example.com");
        assertEquals("active@example.com", response.user().email().toLowerCase(Locale.ROOT));
        assertNotNull(user.getLastLoginAt());
        assertDoesNotThrow(() -> assertNotNull(response.accessToken()));
        assertTrue(user.getLastLoginAt().isAfter(beforeLogin));
    }

    private JwtService jwtService() {
        byte[] secret = "01234567890123456789012345678901".getBytes();
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(new SecretKeySpec(secret, "HmacSHA256")));
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(secret, "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        return new JwtService(
                encoder,
                decoder,
                new JwtProperties("users-service", java.util.Base64.getEncoder().encodeToString(secret), Duration.ofMinutes(30)));
    }
}
