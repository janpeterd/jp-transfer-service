package com.janpeterdhalle.transfer.controllers;

import com.janpeterdhalle.transfer.models.SharedLink;
import com.janpeterdhalle.transfer.repositories.SharedLinkRepository;
import com.janpeterdhalle.transfer.services.FileDownloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/download")
@RequiredArgsConstructor
@Slf4j
public class FileDownloadController {
    private final FileDownloadService fileDownloadService;
    private final SharedLinkRepository sharedLinkRepository;

    @GetMapping
    public ResponseEntity<Resource> handleFileDownload(@RequestParam String downloadName,
                                                       @RequestParam String email) {
        log.info("Download request received for name: {} and email: {}", downloadName, email);
        Optional<SharedLink> sharedLinkOptional = sharedLinkRepository.findSharedLinksByFileNameAndOwnerMailBase64(
                downloadName,
                email);
        if (sharedLinkOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        sharedLinkOptional.get().setDownloads(sharedLinkOptional.get().getDownloads() + 1);
        sharedLinkRepository.save(sharedLinkOptional.get());
        return fileDownloadService.downloadFile(downloadName, email);
    }
}
