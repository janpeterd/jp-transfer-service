package com.janpeterdhalle.transfer.controllers;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.janpeterdhalle.transfer.UserRequestDto;
import com.janpeterdhalle.transfer.dtos.UserResponseDto;
import com.janpeterdhalle.transfer.services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserResponseDto> getUsers(Authentication authentication) {
        return userService.getUsers();
    }

    @PutMapping("/{id}")
    public UserResponseDto updateUser(Authentication authentication,
            @PathVariable Long id,
            @RequestBody UserRequestDto userRequestDto) {
        return userService.updateUserById(id, userRequestDto);
    }

    @PutMapping("/{id}/password")
    public UserResponseDto updateUserPassword(Authentication authentication,
            @PathVariable Long id,
            @RequestBody String password) {
        return userService.updateUserPasswordById(id, password);
    }

    @DeleteMapping("/{id}")
    public void disableUser(Authentication authentication, @PathVariable Long id) {
        userService.disableUser(id);
    }

    @GetMapping("/current")
    public UserResponseDto getCurrentUser(Authentication authentication) {
        return userService.getCurrent(authentication);
    }
}
