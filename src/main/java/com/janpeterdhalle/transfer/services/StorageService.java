package com.janpeterdhalle.transfer.services;

import com.janpeterdhalle.transfer.models.StorageInfo;
import com.janpeterdhalle.transfer.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final UserRepository userRepository;

    // Get the amout of free storage from filesystem:
    public StorageInfo getStorageInfo() {
        File file = new File(Paths.get("uploads/").toString());
        return StorageInfo.builder()
                          .availableSpace(file.getUsableSpace())
                          .totalSpace(file.getTotalSpace())
                          .usedSpace(file.getTotalSpace() - file.getUsableSpace())
                          .build();
    }
}
