package com.janpeterdhalle.transfer.services;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.janpeterdhalle.transfer.models.Transfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileDownloadService {

    public ResponseEntity<Resource> downloadTransfer(Transfer transfer) {
        try {
            Path filePath = Paths.get(transfer.getUploadPath());

            if (!Files.exists(filePath)) {
                log.error("File not found at path: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            log.info("Serving file: {}", filePath);
            return serveFile(filePath, "transfer-" + transfer.getId() + ".zip");

        } catch (Exception e) {
            log.error("Error processing download", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Serve a file as a downloadable resource
     */
    private ResponseEntity<Resource> serveFile(Path filePath, String fileName) throws MalformedURLException {
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            log.error("Cannot read file: {}", filePath);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
