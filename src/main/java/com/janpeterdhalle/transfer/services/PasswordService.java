package com.janpeterdhalle.transfer.services;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.janpeterdhalle.transfer.exceptions.UserNotFoundException;
import com.janpeterdhalle.transfer.models.LoginCredentials;
import com.janpeterdhalle.transfer.models.PasswordResetCredentials;
import com.janpeterdhalle.transfer.models.User;
import com.janpeterdhalle.transfer.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User resetPasswordMatches(String email, PasswordResetCredentials credentials) {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        if (credentials.getPassword().equals(credentials.getNewPassword())) {
            throw new BadCredentialsException("Cannot set same password");
        }
        if (passwordEncoder.matches(credentials.getPassword(), user.getPassword())) {
            LoginCredentials resetCredentials = LoginCredentials.builder()
                    .email(email)
                    .password(credentials.getNewPassword())
                    .build();
            return resetPassword(resetCredentials);
        }
        throw new BadCredentialsException("Wrong password");
    }

    public User resetPassword(LoginCredentials credentials) {
        User user = userRepository.findByEmail(credentials.getEmail()).orElseThrow(UserNotFoundException::new);
        user.setPassword(passwordEncoder.encode(credentials.getPassword()));
        return userRepository.save(user);
    }
}
