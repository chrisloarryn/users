package com.chrisloarryn.users.service.impl;

import java.time.Instant;
import java.util.Locale;

import com.chrisloarryn.users.domain.User;
import com.chrisloarryn.users.error.BusinessValidationException;
import com.chrisloarryn.users.error.ConflictException;
import com.chrisloarryn.users.error.InvalidCredentialsException;
import com.chrisloarryn.users.repository.UserRepository;
import com.chrisloarryn.users.security.JwtService;
import com.chrisloarryn.users.service.AuthService;
import com.chrisloarryn.users.service.PasswordPolicyValidator;
import com.chrisloarryn.users.service.UserMapper;
import com.chrisloarryn.users.web.dto.request.LoginRequest;
import com.chrisloarryn.users.web.dto.request.RegisterUserRequest;
import com.chrisloarryn.users.web.dto.response.AuthResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PasswordPolicyValidator passwordPolicyValidator,
            UserMapper userMapper,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyValidator = passwordPolicyValidator;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterUserRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("Email already registered");
        }
        validatePassword(request.password());

        User user = new User(request.name().trim(), normalizedEmail, passwordEncoder.encode(request.password()));
        user.replacePhones(userMapper.toPhones(request.phones()));
        user.markLoggedIn(Instant.now());

        User savedUser = userRepository.save(user);
        JwtService.JwtToken jwtToken = jwtService.issueToken(savedUser);
        return new AuthResponse("Bearer", jwtToken.value(), jwtToken.expiresAt(), userMapper.toResponse(savedUser));
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .filter(User::isActive)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        user.markLoggedIn(Instant.now());
        User savedUser = userRepository.save(user);
        JwtService.JwtToken jwtToken = jwtService.issueToken(savedUser);
        return new AuthResponse("Bearer", jwtToken.value(), jwtToken.expiresAt(), userMapper.toResponse(savedUser));
    }

    private void validatePassword(String password) {
        if (!passwordPolicyValidator.isValid(password)) {
            throw new BusinessValidationException(
                    "Password must contain upper and lower case letters, a number, a special character and at least 8 characters");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
