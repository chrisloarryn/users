package com.chrisloarryn.users.service.impl;

import java.util.UUID;

import com.chrisloarryn.users.domain.User;
import com.chrisloarryn.users.error.InvalidCredentialsException;
import com.chrisloarryn.users.repository.UserRepository;
import com.chrisloarryn.users.service.CurrentUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecurityContextCurrentUserService implements CurrentUserService {

    private final UserRepository userRepository;

    public SecurityContextCurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        return userRepository.findByIdAndActiveTrue(getCurrentUserId())
                .orElseThrow(() -> new InvalidCredentialsException("Authenticated user not found"));
    }

    @Override
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidCredentialsException("Authentication required");
        }

        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException ex) {
            throw new InvalidCredentialsException("Authentication required");
        }
    }
}
