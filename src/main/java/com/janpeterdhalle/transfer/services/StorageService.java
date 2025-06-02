package com.janpeterdhalle.transfer.services;

import java.io.File;
import java.nio.file.Paths;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.janpeterdhalle.transfer.models.StorageInfo;
import com.janpeterdhalle.transfer.models.User;
import com.janpeterdhalle.transfer.repositories.FileRepository;
import com.janpeterdhalle.transfer.repositories.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StorageService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final FileRepository fileRepository;

    // Get the amout of free storage from filesystem:
    public StorageInfo getStorageInfo() {
        File file = new File(Paths.get("uploads/").toString());
        return StorageInfo.builder()
                .availableSpace(file.getUsableSpace())
                .totalSpace(file.getTotalSpace())
                .usedSpace(file.getTotalSpace() - file.getUsableSpace())
                .build();
    }

    public Long getUserStorageInfo(Authentication authentication) {
        User user = userService.getLoggedInUser(authentication);
        return fileRepository.getFilesizeByUserAndUploadedTrue(user);
    }
}
