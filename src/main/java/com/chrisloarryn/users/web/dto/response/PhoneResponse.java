package com.chrisloarryn.users.web.dto.response;

import java.time.Instant;

public record PhoneResponse(
        String number,
        String cityCode,
        String countryCode,
        Instant createdAt) {
}
