package com.janpeterdhalle.transfer.services;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.janpeterdhalle.transfer.models.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class JwtService {
    private final JwtEncoder jwtEncoder;
    @Value("${jwt.jwt-token.ttl}")
    private Duration jwtTokenTtl;

    public String generateToken(final User user) {
        var role = user.getRole();

        final var claimsSet = JwtClaimsSet.builder()
                .subject(user.getEmail())
                .claim("email", user.getEmail())
                .claim("scope", List.of(role))
                .issuer("JP transfer")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(jwtTokenTtl))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet))
                .getTokenValue();
    }
}
