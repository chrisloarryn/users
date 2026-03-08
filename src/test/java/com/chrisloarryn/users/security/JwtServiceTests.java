package com.chrisloarryn.users.security;

import java.time.Duration;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

import com.chrisloarryn.users.config.JwtProperties;
import com.chrisloarryn.users.domain.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
}
