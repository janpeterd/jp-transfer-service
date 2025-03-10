package com.janpeterdhalle.transfer.controllers;

import com.janpeterdhalle.transfer.models.*;
import com.janpeterdhalle.transfer.repositories.UserRepository;
import com.janpeterdhalle.transfer.services.AuthService;
import com.janpeterdhalle.transfer.services.PasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final AuthService authService;
    private final PasswordService passwordService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public AuthResponse registerHandler(@RequestBody User user) {
        // Check if user doesnt already exist:
        Optional<User> optionalUser = userRepository.findByEmail(user.getEmail());
        if (optionalUser.isPresent()) {
            throw new RuntimeException("User already exists");
        }
        String encodedPass = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPass);
        user.setRole(Role.USER);
        user = userRepository.save(user);
        String token = authService.generateToken(user);
        return new AuthResponse(token, user.getEmail(), user.getRole(), true);
    }

    @PostMapping("/login")
    public AuthResponse loginHandler(@RequestBody LoginCredentials loginCredentials) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
            loginCredentials.getEmail(),
            loginCredentials.getPassword()));
        log.info("Authorities for user", SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        Optional<User> optionalUser = userRepository.findByEmail(loginCredentials.getEmail());
        if (optionalUser.isEmpty())
            throw new RuntimeException("User not found");
        String token = authService.generateToken(optionalUser.get());
        return new AuthResponse(token, loginCredentials.getEmail(), optionalUser.get().getRole(), true);
    }

    @PostMapping("/change-password")
    public User handleResetPassword(@AuthenticationPrincipal(errorOnInvalidType = true) String email,
                                    @RequestBody PasswordResetCredentials passwordResetCredentials) {
        log.info("Changing password for {}", email);
        log.info("NEW PASSWORD {}", passwordResetCredentials.getNewPassword());
        log.info("OLD PASSWORD {}", passwordResetCredentials.getPassword());
        return passwordService.resetPasswordMatches(email, passwordResetCredentials);
    }

    /* backup endpoint for admin to set a users password */
    @PostMapping("/set-password")
    public User handleSetPassword(@RequestBody LoginCredentials passwordResetCredentials) {
        return passwordService.resetPassword(passwordResetCredentials);
    }
}
