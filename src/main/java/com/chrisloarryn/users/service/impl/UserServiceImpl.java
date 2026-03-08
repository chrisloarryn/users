package com.chrisloarryn.users.service.impl;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.chrisloarryn.users.domain.User;
import com.chrisloarryn.users.error.ConflictException;
import com.chrisloarryn.users.error.NotFoundException;
import com.chrisloarryn.users.repository.UserRepository;
import com.chrisloarryn.users.service.PasswordPolicyValidator;
import com.chrisloarryn.users.service.UserMapper;
import com.chrisloarryn.users.service.UserService;
import com.chrisloarryn.users.web.dto.request.UpdateUserRequest;
import com.chrisloarryn.users.web.dto.response.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator passwordPolicyValidator;

    public UserServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            PasswordPolicyValidator passwordPolicyValidator) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyValidator = passwordPolicyValidator;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAllByActiveTrueOrderByCreatedAtAsc().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return userMapper.toResponse(getActiveUser(id));
    }

    @Override
    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = getActiveUser(id);
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, id)) {
            throw new ConflictException("Email already registered");
        }

        user.updateProfile(request.name().trim(), normalizedEmail);
        user.replacePhones(userMapper.toPhones(request.phones()));

        if (request.password() != null) {
            String candidate = request.password().trim();
            if (candidate.isEmpty() || !passwordPolicyValidator.isValid(candidate)) {
                throw new IllegalArgumentException(
                        "Password must contain upper and lower case letters, a number, a special character and at least 8 characters");
            }
            user.updatePasswordHash(passwordEncoder.encode(candidate));
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = getActiveUser(id);
        user.deactivate();
        userRepository.save(user);
    }

    private User getActiveUser(UUID id) {
        return userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
