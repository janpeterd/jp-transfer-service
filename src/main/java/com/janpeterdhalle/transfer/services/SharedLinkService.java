package com.janpeterdhalle.transfer.services;

import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.janpeterdhalle.transfer.Utils;
import com.janpeterdhalle.transfer.models.SharedLink;
import com.janpeterdhalle.transfer.models.Transfer;
import com.janpeterdhalle.transfer.repositories.SharedLinkRepository;
import com.janpeterdhalle.transfer.repositories.TransferRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SharedLinkService {
    private final SharedLinkRepository sharedLinkRepository;
    private final Utils utils;
    private final TransferRepository transferRepository;

    public void create(Transfer transfer) {
        var uuid = UUID.randomUUID();
        SharedLink link = sharedLinkRepository.save(SharedLink.builder()
                .user(transfer.getUser())
                .maxDownloads(100)
                .url(utils.getUploadPath(transfer).toString())
                .uuid(uuid.toString())
                .downloadLink("/download/" + uuid)
                .transfer(transfer)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build());
        transfer.setSharedLink(link);
        transferRepository.save(transfer);
    }

    public void deletedAssociatedData(SharedLink sharedLink) {
        Utils.deleteDirectory(new File(utils.getUploadPath(sharedLink.getTransfer()).toUri()));
    }
}
