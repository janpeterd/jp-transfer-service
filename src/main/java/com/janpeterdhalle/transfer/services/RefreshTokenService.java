package com.janpeterdhalle.transfer.services;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.janpeterdhalle.transfer.exceptions.RefreshTokenNotFoundException;
import com.janpeterdhalle.transfer.exceptions.TokenExpiredException;
import com.janpeterdhalle.transfer.exceptions.UserNotFoundException;
import com.janpeterdhalle.transfer.models.AuthResponse;
import com.janpeterdhalle.transfer.models.RefreshToken;
import com.janpeterdhalle.transfer.models.User;
import com.janpeterdhalle.transfer.repositories.RefreshTokenRepository;
import com.janpeterdhalle.transfer.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional(noRollbackFor = TokenExpiredException.class, propagation = Propagation.REQUIRES_NEW)
    public AuthResponse refreshToken(UUID refreshToken) {
        RefreshToken refreshTokenEntity = refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new BadCredentialsException(
                        "Invalid or expired refresh token"));
        if (refreshTokenEntity.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenEntity.getUser().setRefreshToken(null);
            userRepository.save(refreshTokenEntity.getUser());
            throw new TokenExpiredException("RefreshToken is expired.", refreshTokenEntity.getExpiresAt());
        }

        final String newAccessToken = jwtService.generateToken(refreshTokenEntity.getUser());
        return new AuthResponse(newAccessToken, refreshToken.toString());
    }

    // Delete tokens after logout
    public void revokeRefreshToken(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(UserNotFoundException::new);
        RefreshToken token = refreshTokenRepository.findByUser(user)
                .orElseThrow(RefreshTokenNotFoundException::new);
        user.setRefreshToken(null);
        userRepository.save(user);
        refreshTokenRepository.delete(token);
    }
}
