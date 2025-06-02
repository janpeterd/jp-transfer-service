package com.janpeterdhalle.transfer.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.janpeterdhalle.transfer.models.RefreshToken;
import com.janpeterdhalle.transfer.models.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findById(UUID refreshToken);

    Optional<RefreshToken> findByUser(User user);

    void deleteByUser(User user);
}
