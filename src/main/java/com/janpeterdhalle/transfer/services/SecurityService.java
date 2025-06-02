package com.janpeterdhalle.transfer.services;

import com.janpeterdhalle.transfer.models.Transfer;
import com.janpeterdhalle.transfer.models.User;
import com.janpeterdhalle.transfer.repositories.TransferRepository;
import com.janpeterdhalle.transfer.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class SecurityService {
    private final TransferRepository transferRepository;
    private final UserRepository userRepository;

    public Boolean isOwnerOfTransfer(Long id, Authentication authentication) {
        if (authentication.isAuthenticated()) {
            Optional<User> user = userRepository.findByEmail(authentication.getName());

            Optional<Transfer> transfer = transferRepository.findById(id);

            if (user.isPresent() && transfer.isPresent()) {
                return transfer.get().getUser().equals(user.get());
            }
        }
        return false;
    }
}
