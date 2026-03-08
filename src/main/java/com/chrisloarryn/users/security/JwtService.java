package com.chrisloarryn.users.security;

import java.time.Instant;
import java.util.UUID;

import com.chrisloarryn.users.config.JwtProperties;
import com.chrisloarryn.users.domain.User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties jwtProperties;

    public JwtService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.jwtProperties = jwtProperties;
    }

    public JwtToken issueToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.expiration());
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("email", user.getEmail())
                .claim("scope", "users")
                .build();

        String tokenValue = jwtEncoder.encode(
                        JwtEncoderParameters.from(
                                JwsHeader.with(MacAlgorithm.HS256).type("JWT").build(),
                                claimsSet))
                .getTokenValue();

        return new JwtToken(tokenValue, expiresAt);
    }

    public UUID extractUserId(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return UUID.fromString(jwt.getSubject());
    }

    public record JwtToken(String value, Instant expiresAt) {
    }
}
