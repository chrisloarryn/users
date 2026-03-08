package com.chrisloarryn.users.service;

import java.util.Optional;
import java.util.UUID;

import com.chrisloarryn.users.domain.User;
import com.chrisloarryn.users.error.InvalidCredentialsException;
import com.chrisloarryn.users.repository.UserRepository;
import com.chrisloarryn.users.service.impl.SecurityContextCurrentUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrentUserServiceTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CurrentUserService currentUserService = new SecurityContextCurrentUserService(userRepository);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolvesTheAuthenticatedActiveUser() {
        User user = new User("Jane Doe", "jane@example.com", "encoded");
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(user, "id", userId);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), null, of()));
        when(userRepository.findByIdAndActiveTrue(userId)).thenReturn(Optional.of(user));

        assertEquals(userId, currentUserService.getCurrentUser().getId());
    }

    @Test
    void rejectsMissingAuthentication() {
        assertThrows(InvalidCredentialsException.class, currentUserService::getCurrentUser);
    }
}
