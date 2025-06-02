package com.janpeterdhalle.transfer.controllers;

import com.janpeterdhalle.transfer.exceptions.SharedLinkNotFoundException;
import com.janpeterdhalle.transfer.models.SharedLink;
import com.janpeterdhalle.transfer.repositories.SharedLinkRepository;
import com.janpeterdhalle.transfer.services.FileDownloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/download")
@RequiredArgsConstructor
@Slf4j
public class FileDownloadController {
    private final FileDownloadService fileDownloadService;
    private final SharedLinkRepository sharedLinkRepository;

    @GetMapping("/{uuid}")
    public ResponseEntity<Resource> handleFileDownload(@PathVariable String uuid) {
        log.info("Download request received for uuid: {}", uuid);
        SharedLink sharedLink = sharedLinkRepository.findSharedLinkByUuid(uuid)
                                                    .orElseThrow(SharedLinkNotFoundException::new);
        sharedLink.setDownloads(sharedLink.getDownloads() + 1);
        sharedLinkRepository.save(sharedLink);
        return fileDownloadService.downloadTransfer(sharedLink.getTransfer());
    }
}
