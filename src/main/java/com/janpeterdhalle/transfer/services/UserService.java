package com.janpeterdhalle.transfer.services;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.janpeterdhalle.transfer.Constants;
import com.janpeterdhalle.transfer.UserRequestDto;
import com.janpeterdhalle.transfer.Utils;
import com.janpeterdhalle.transfer.dtos.UserResponseDto;
import com.janpeterdhalle.transfer.exceptions.FileEntityNotFoundException;
import com.janpeterdhalle.transfer.exceptions.UserNotFoundException;
import com.janpeterdhalle.transfer.mappers.UserMapper;
import com.janpeterdhalle.transfer.models.Role;
import com.janpeterdhalle.transfer.models.User;
import com.janpeterdhalle.transfer.repositories.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final Environment environment;
    @Value("${admin.username}")
    private String adminUsername;
    @Value("${admin.email}")
    private String adminEmail;
    @Value("${admin.password}")
    private String adminPassword;

    @PostConstruct
    public void init() {
        if (ArrayUtils.contains(environment.getActiveProfiles(), "dev")) {
            if (userRepository.findByEmail("admin@example.com").isEmpty()) {
                User user = new User();
                user.setEmail("admin@example.com");
                user.setPassword(passwordEncoder.encode("admin"));
                user.setRole(Role.ADMIN);
                user.setActive(true);
                userRepository.save(user);
            }
            if (userRepository.findByEmail("user@example.com").isEmpty()) {
                User user = new User();
                user.setEmail("user@example.com");
                user.setPassword(passwordEncoder.encode("user"));
                user.setActive(true);
                user.setRole(Role.USER);
                userRepository.save(user);
            }
        } else {
            User user = new User();
            user.setEmail(adminEmail);
            user.setUsername(adminUsername);
            user.setActive(true);
            user.setPassword(passwordEncoder.encode(adminPassword));
            user.setRole(Role.ADMIN);
            userRepository.save(user);
        }
    }

    public User getLoggedInUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName()).orElseThrow(FileEntityNotFoundException::new);
    }

    public List<UserResponseDto> getUsers() {
        return userRepository.findAllByActiveTrue().stream().map(userMapper::toDto).toList();
    }

    public UserResponseDto getUser(Long id) {
        return userMapper.toDto(userRepository.findById(id).orElseThrow(FileEntityNotFoundException::new));
    }

    public UserResponseDto updateUserById(Long id, UserRequestDto userRequestDto) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        userMapper.partialUpdate(userRequestDto, user);
        return userMapper.toDto(userRepository.save(user));
    }

    public UserResponseDto updateUserPasswordById(Long id, String password) {
        if (!StringUtils.hasLength(password))
            throw new BadCredentialsException("Invalid password");
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        user.setPassword(passwordEncoder.encode(password));
        return userMapper.toDto(userRepository.save(user));
    }

    public UserResponseDto disableUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        File dirToDelete = new File(Utils.getUploadPath(user).toString());
        if (StringUtils.hasLength(dirToDelete.getAbsolutePath()) && dirToDelete.exists() && dirToDelete.isDirectory()
                && !Constants.forbiddenDeleteDirs.contains(
                        dirToDelete.getAbsolutePath())) {
            Utils.deleteDirectory(dirToDelete);
        } else {
            log.error("Delete transfer failed invalid file/dir: {}", dirToDelete.getAbsolutePath());
        }
        user.setActive(false);
        return userMapper.toDto(userRepository.save(user));
    }

    public UserResponseDto getCurrent(Authentication authentication) {
        return userMapper.toDto(getLoggedInUser(authentication));
    }
}
