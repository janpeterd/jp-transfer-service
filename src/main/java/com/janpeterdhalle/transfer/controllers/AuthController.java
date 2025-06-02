package com.janpeterdhalle.transfer.controllers;

import com.janpeterdhalle.transfer.dtos.UserResponseDto;
import com.janpeterdhalle.transfer.models.AuthResponse;
import com.janpeterdhalle.transfer.models.LoginCredentials;
import com.janpeterdhalle.transfer.models.PasswordResetCredentials;
import com.janpeterdhalle.transfer.models.User;
import com.janpeterdhalle.transfer.services.AuthService;
import com.janpeterdhalle.transfer.services.PasswordService;
import com.janpeterdhalle.transfer.services.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final PasswordService passwordService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public UserResponseDto registerHandler(@RequestBody User user) {
        return authService.registerUser(user);
    }

    @PostMapping("/login")
    public AuthResponse loginHandler(@RequestBody LoginCredentials loginCredentials) {
        return authService.login(loginCredentials);
    }

    @PostMapping("/change-password")
    public User handleResetPassword(
        Authentication authentication,
        @RequestBody PasswordResetCredentials passwordResetCredentials) {
        return passwordService.resetPasswordMatches(authentication.getName(), passwordResetCredentials);
    }

    /* backup endpoint for admin to set a users password */
    @PostMapping("/set-password")
    public User handleSetPassword(@RequestBody LoginCredentials passwordResetCredentials) {
        return passwordService.resetPassword(passwordResetCredentials);
    }

    @Operation(summary = "Get a refresh token")
    @PostMapping("/refresh-token")
    public AuthResponse refreshToken(@RequestParam UUID refreshToken) {
        return refreshTokenService.refreshToken(refreshToken);
    }
}
