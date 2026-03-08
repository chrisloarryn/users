package com.chrisloarryn.users.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PhoneRequest(
        @NotBlank @Size(max = 40) String number,
        @NotBlank @Size(max = 10) String cityCode,
        @NotBlank @Size(max = 10) String countryCode) {
}
