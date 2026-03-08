package com.chrisloarryn.users.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.chrisloarryn.users.domain.Phone;
import com.chrisloarryn.users.domain.User;
import com.chrisloarryn.users.error.BusinessValidationException;
import com.chrisloarryn.users.error.NotFoundException;
import com.chrisloarryn.users.repository.UserRepository;
import com.chrisloarryn.users.service.impl.UserMapperImpl;
import com.chrisloarryn.users.service.impl.UserServiceImpl;
import com.chrisloarryn.users.web.dto.request.PhoneRequest;
import com.chrisloarryn.users.web.dto.request.UpdateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTests {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private PasswordPolicyValidator passwordPolicyValidator;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        passwordPolicyValidator = mock(PasswordPolicyValidator.class);
        userService = new UserServiceImpl(
                userRepository,
                new UserMapperImpl(),
                passwordEncoder,
                passwordPolicyValidator);
    }

    @Test
    void getAllReturnsOnlyActiveUsersProvidedByTheRepositoryQuery() {
        User activeUser = persistedUser("Jane Doe", "jane@example.com", "encoded");
        when(userRepository.findAllByActiveTrueOrderByCreatedAtAsc()).thenReturn(List.of(activeUser));

        var users = userService.getAll();

        assertEquals(1, users.size());
        assertEquals("jane@example.com", users.getFirst().email());
        assertEquals(true, users.getFirst().active());
    }

    @Test
    void updateWithoutPasswordKeepsTheExistingPasswordHash() {
        UUID userId = UUID.randomUUID();
        User user = persistedUser("Jane Doe", "jane@example.com", passwordEncoder.encode("StrongPass1!"));
        String previousHash = user.getPasswordHash();
        when(userRepository.findByIdAndActiveTrue(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("updated@example.com", userId)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userService.update(
                userId,
                new UpdateUserRequest(
                        " Updated Name ",
                        " UPDATED@example.com ",
                        null,
                        List.of(new PhoneRequest("999999999", "2", "56"))));

        assertEquals("updated@example.com", response.email());
        assertEquals("Updated Name", response.name());
        assertEquals(previousHash, user.getPasswordHash());
    }

    @Test
    void updateRejectsBlankPasswords() {
        UUID userId = UUID.randomUUID();
        User user = persistedUser("Jane Doe", "jane@example.com", passwordEncoder.encode("StrongPass1!"));
        when(userRepository.findByIdAndActiveTrue(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("updated@example.com", userId)).thenReturn(false);

        assertThrows(
                BusinessValidationException.class,
                () -> userService.update(
                        userId,
                        new UpdateUserRequest(
                                "Updated Name",
                                "updated@example.com",
                                "   ",
                                List.of(new PhoneRequest("999999999", "2", "56")))));
    }

    @Test
    void getByIdRejectsInactiveUsers() {
        assertThrows(NotFoundException.class, () -> userService.getById(UUID.randomUUID()));
    }

    @Test
    void deleteRejectsInactiveUsers() {
        assertThrows(NotFoundException.class, () -> userService.delete(UUID.randomUUID()));
    }

    @Test
    void deleteDeactivatesTheUser() {
        UUID userId = UUID.randomUUID();
        User user = persistedUser("Jane Doe", "jane@example.com", passwordEncoder.encode("StrongPass1!"));
        when(userRepository.findByIdAndActiveTrue(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.delete(userId);

        assertFalse(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void updatePasswordReplacesTheStoredHash() {
        UUID userId = UUID.randomUUID();
        User user = persistedUser("Jane Doe", "jane@example.com", passwordEncoder.encode("StrongPass1!"));
        String previousHash = user.getPasswordHash();
        when(userRepository.findByIdAndActiveTrue(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("updated@example.com", userId)).thenReturn(false);
        when(passwordPolicyValidator.isValid("NewStrong1!")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.update(
                userId,
                new UpdateUserRequest(
                        "Updated Name",
                        "updated@example.com",
                        "NewStrong1!",
                        List.of(new PhoneRequest("999999999", "2", "56"))));

        assertNotEquals(previousHash, user.getPasswordHash());
    }

    private User persistedUser(String name, String email, String passwordHash) {
        User user = new User(name, email, passwordHash);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(user, "createdAt", Instant.parse("2026-03-08T00:00:00Z"));
        ReflectionTestUtils.setField(user, "updatedAt", Instant.parse("2026-03-08T00:00:00Z"));
        user.replacePhones(List.of(new Phone("123456789", "1", "56")));
        for (Phone phone : user.getPhones()) {
            ReflectionTestUtils.setField(phone, "createdAt", Instant.parse("2026-03-08T00:00:00Z"));
        }
        return user;
    }
}
