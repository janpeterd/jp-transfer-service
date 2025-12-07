package com.janpeterdhalle.transfer.services;

import java.time.Instant;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import com.janpeterdhalle.transfer.models.User;
import com.janpeterdhalle.transfer.repositories.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;

    @Cacheable(value = "userSync", key = "#subject + ':' + #issuedAt.toEpochMilli()")
    public boolean syncUserOnce(String subject,
            String username,
            String email,
            Instant issuedAt) {

        User user = userRepository.findBySubject(subject)
                .orElseGet(() -> User.builder()
                        .subject(subject)
                        .build());

        user.setUsername(username);
        user.setEmail(email);

        userRepository.save(user); // Save acts as UPSERT

        return true;
    }

    public Optional<User> getMe(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return userRepository.findBySubject(jwt.getSubject());
        }
        return Optional.empty();
    }

    public Optional<User> findBySubject(String subject) {
        return userRepository.findBySubject(subject);
    }
}
