package com.janpeterdhalle.transfer.services;

import com.janpeterdhalle.transfer.models.LoginCredentials;
import com.janpeterdhalle.transfer.models.PasswordResetCredentials;
import com.janpeterdhalle.transfer.models.User;
import com.janpeterdhalle.transfer.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User resetPasswordMatches(String email, PasswordResetCredentials credentials) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        if (credentials.getPassword().equals(credentials.getNewPassword())) {
            throw new RuntimeException("Cannot set same password");
        }
        if (passwordEncoder.matches(credentials.getPassword(), user.get().getPassword())) {
            LoginCredentials resetCredentials = LoginCredentials.builder()
                                                                .email(email)
                                                                .password(credentials.getNewPassword())
                                                                .build();
            return resetPassword(resetCredentials);
        }
        log.error("Passwords dont match {} and {}", credentials.getPassword(), user.get().getPassword());
        throw new RuntimeException("Wrong password");
    }

    public User resetPassword(LoginCredentials credentials) {
        Optional<User> user = userRepository.findByEmail(credentials.getEmail());
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        // set the new password
        user.get().setPassword(passwordEncoder.encode(credentials.getPassword()));
        return userRepository.save(user.get());
    }
}
