package com.chrisloarryn.users.web.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        List<PhoneResponse> phones,
        Instant createdAt,
        Instant updatedAt,
        Instant lastLoginAt,
        boolean active) {
}
