package com.chrisloarryn.users.security;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

import com.chrisloarryn.users.config.JwtProperties;
import com.chrisloarryn.users.domain.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTests {

    @Test
    void issuesTokensContainingTheUserId() {
        byte[] secret = "01234567890123456789012345678901".getBytes();
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(new SecretKeySpec(secret, "HmacSHA256")));
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(secret, "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        JwtService jwtService = new JwtService(
                encoder,
                decoder,
                new JwtProperties("users-service", java.util.Base64.getEncoder().encodeToString(secret), Duration.ofMinutes(30)));
        User user = new User("Jane Doe", "jane@example.com", "encoded");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        JwtService.JwtToken token = jwtService.issueToken(user);

        assertNotNull(token.value());
        assertNotNull(token.expiresAt());
        assertEquals(user.getId(), jwtService.extractUserId(token.value()));
    }

    @Test
    void issuedTokensContainConfiguredIssuerAndExpiration() {
        byte[] secret = "01234567890123456789012345678901".getBytes();
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(new SecretKeySpec(secret, "HmacSHA256")));
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(secret, "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        JwtService jwtService = new JwtService(
                encoder,
                decoder,
                new JwtProperties("custom-issuer", java.util.Base64.getEncoder().encodeToString(secret), Duration.ofMinutes(15)));
        User user = new User("Jane Doe", "jane@example.com", "encoded");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        JwtService.JwtToken token = jwtService.issueToken(user);
        Jwt jwt = decoder.decode(token.value());

        assertEquals("custom-issuer", jwt.getClaims().get("iss"));
        assertEquals(user.getEmail(), jwt.getClaimAsString("email"));
        assertTrue(jwt.getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    void rejectsTokensSignedWithADifferentSecret() {
        byte[] validSecret = "01234567890123456789012345678901".getBytes();
        byte[] invalidSecret = "abcdefghijabcdefghijabcdefghij12".getBytes();
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(new SecretKeySpec(validSecret, "HmacSHA256")));
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(invalidSecret, "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        JwtService jwtService = new JwtService(
                encoder,
                decoder,
                new JwtProperties("users-service", java.util.Base64.getEncoder().encodeToString(validSecret), Duration.ofMinutes(30)));
        User user = new User("Jane Doe", "jane@example.com", "encoded");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        JwtService.JwtToken token = jwtService.issueToken(user);

        assertThrows(BadJwtException.class, () -> jwtService.extractUserId(token.value()));
    }

    @Test
    void rejectsExpiredTokens() {
        byte[] secret = "01234567890123456789012345678901".getBytes();
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(new SecretKeySpec(secret, "HmacSHA256")));
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(secret, "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        JwtService jwtService = new JwtService(
                encoder,
                token -> {
                    throw new BadJwtException("expired");
                },
                new JwtProperties("users-service", java.util.Base64.getEncoder().encodeToString(secret), Duration.ofMinutes(30)));

        assertThrows(BadJwtException.class, () -> jwtService.extractUserId("expired-token"));
    }
}
