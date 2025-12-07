package com.janpeterdhalle.transfer.services;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import com.janpeterdhalle.transfer.models.Transfer;
import com.janpeterdhalle.transfer.models.User;
import com.janpeterdhalle.transfer.repositories.TransferRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class SecurityService {
    private TransferRepository transferRepository;
    private UserService userService;

    public Boolean isOwnerOfTransfer(Long id, Authentication authentication) {
        if (!authentication.isAuthenticated()) {
            return false;
        }

        Optional<User> user = userService.findBySubject(
                ((JwtAuthenticationToken) authentication).getToken().getSubject());

        Optional<Transfer> transfer = transferRepository.findById(id);

        return user.isPresent()
                && transfer.isPresent()
                && transfer.get().getUser().getId().equals(user.get().getId());
    }
}
