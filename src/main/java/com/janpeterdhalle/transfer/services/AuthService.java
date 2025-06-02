package com.janpeterdhalle.transfer.services;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.janpeterdhalle.transfer.dtos.UserResponseDto;
import com.janpeterdhalle.transfer.exceptions.RefreshTokenNotFoundException;
import com.janpeterdhalle.transfer.exceptions.UserExistsException;
import com.janpeterdhalle.transfer.mappers.UserMapper;
import com.janpeterdhalle.transfer.models.*;
import com.janpeterdhalle.transfer.repositories.RefreshTokenRepository;
import com.janpeterdhalle.transfer.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final UserAuthenticationService userAuthenticationService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    @Value("${jwt.refreshToken.ttl}")
    private Duration refreshTokenTtl;
    public static final String INVALID_USER_EMAIL_OR_PASSWORD = "Invalid user email or password";

    public UserResponseDto registerUser(User user) {
        userRepository.findByEmail(user.getEmail()).ifPresent(s -> {
            throw new UserExistsException("User already exists");
        });
        String encodedPass = userAuthenticationService.encodePassword(user.getPassword());
        user.setPassword(encodedPass);
        user.setRole(Role.USER);
        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    public AuthResponse login(LoginCredentials loginDTO) {
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new BadCredentialsException(INVALID_USER_EMAIL_OR_PASSWORD));
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            log.error(INVALID_USER_EMAIL_OR_PASSWORD);
            throw new BadCredentialsException(INVALID_USER_EMAIL_OR_PASSWORD);
        }

        if (user.getRefreshToken() == null) {
            // Create a new refreshtoken if the user doesn't have one yet
            return getLoginResponseDTO(user);

        } else {
            RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                    .orElseThrow(RefreshTokenNotFoundException::new);

            // Check if the refreshtoken is expired
            if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
                user.setRefreshToken(null);
                userRepository.save(user);

                // Create a new refreshtoken if the token is expired
                return getLoginResponseDTO(user);
            }

            return refreshTokenService.refreshToken(refreshToken.getId());
        }
    }

    private AuthResponse getLoginResponseDTO(User user) {
        final String token = jwtService.generateToken(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setExpiresAt(Instant.now().plus(refreshTokenTtl));
        refreshToken.setUser(user);
        refreshTokenRepository.save(refreshToken);
        userRepository.save(user);

        return new AuthResponse(token, refreshToken.toString());
    }

}
