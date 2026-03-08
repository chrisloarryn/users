package com.chrisloarryn.users.service;

import java.time.Instant;
import java.util.List;

import com.chrisloarryn.users.domain.Phone;
import com.chrisloarryn.users.domain.User;
import com.chrisloarryn.users.service.impl.UserMapperImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UserMapperTests {

    private final UserMapper userMapper = new UserMapperImpl();

    @Test
    void mapsUserWithoutExposingPasswordData() {
        User user = new User("Jane Doe", "jane@example.com", "encoded");
        user.markLoggedIn(Instant.parse("2026-01-01T00:00:00Z"));
        user.replacePhones(List.of(new Phone("123456789", "1", "56")));

        var response = userMapper.toResponse(user);

        assertEquals("Jane Doe", response.name());
        assertEquals("jane@example.com", response.email());
        assertEquals(1, response.phones().size());
        assertFalse(response.active() && response.phones().isEmpty());
    }
}
