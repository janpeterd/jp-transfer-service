package com.janpeterdhalle.transfer.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.janpeterdhalle.transfer.models.LoginCredentials;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public void AuthenticateUser(LoginCredentials loginCredentials) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginCredentials.getEmail(),
                loginCredentials.getPassword()));
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
