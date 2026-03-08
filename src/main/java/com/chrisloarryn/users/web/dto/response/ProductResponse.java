package com.chrisloarryn.users.web.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        BigDecimal price,
        Instant createdAt,
        Instant updatedAt,
        UUID createdByUserId,
        UUID updatedByUserId) {
}
