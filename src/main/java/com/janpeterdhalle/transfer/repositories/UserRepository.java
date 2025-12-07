package com.janpeterdhalle.transfer.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.janpeterdhalle.transfer.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String name);

    Optional<User> findBySubject(String subject);
}
