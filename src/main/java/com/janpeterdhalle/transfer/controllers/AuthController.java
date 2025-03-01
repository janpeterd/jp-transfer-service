package com.janpeterdhalle.transfer.controllers;

import com.janpeterdhalle.transfer.models.AuthResponse;
import com.janpeterdhalle.transfer.models.LoginCredentials;
import com.janpeterdhalle.transfer.models.Role;
import com.janpeterdhalle.transfer.models.User;
import com.janpeterdhalle.transfer.repositories.UserRepository;
import com.janpeterdhalle.transfer.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerHandler(@RequestBody User user) {
        // Check if user doesnt already exist:
        Optional<User> optionalUser = userRepository.findByEmail(user.getEmail());
        if (optionalUser.isPresent()) {
            return ResponseEntity.badRequest().body(null);
        }
        String encodedPass = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPass);
        user.setRole(Role.USER);
        user = userRepository.save(user);
        String token = authService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), user.getRole(), true));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginHandler(@RequestBody LoginCredentials loginCredentials) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginCredentials.getEmail(),
                                                                                                        loginCredentials.getPassword()));
            Optional<User> optionalUser = userRepository.findByEmail(loginCredentials.getEmail());
            if (optionalUser.isEmpty())
                return ResponseEntity.notFound().build();
            String token = authService.generateToken(optionalUser.get());
            return ResponseEntity.ok(new AuthResponse(token,
                                                      loginCredentials.getEmail(),
                                                      optionalUser.get().getRole(),
                                                      true));

        } catch (AuthenticationException authenticationException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new AuthResponse("", "", null, false)
                                                                      );
        }
    }
}
