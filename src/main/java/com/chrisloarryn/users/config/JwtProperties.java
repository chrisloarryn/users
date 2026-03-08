package com.chrisloarryn.users.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String issuer, String secret, Duration expiration) {
}
