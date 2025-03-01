package com.janpeterdhalle.transfer.services;

import com.janpeterdhalle.transfer.models.SharedLink;
import com.janpeterdhalle.transfer.repositories.SharedLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileDownloadService {

    private final SharedLinkRepository sharedLinkRepository;

    /**
     * Download a file based on downloadName and email
     *
     * @param downloadName The name used during upload
     * @param encodedEmail The user's email base64
     * @return ResponseEntity with the file resource
     */
    public ResponseEntity<Resource> downloadFile(String downloadName, String encodedEmail) {
        try {
            // Base64 encode email to match storage format
            // Find the shared link in the database
            Optional<SharedLink> sharedLink = sharedLinkRepository.findSharedLinksByFileNameAndOwnerMailBase64(
                    downloadName, encodedEmail);

            if (!sharedLink.isPresent()) {
                log.error("No shared link found for {} and email {}", downloadName, encodedEmail);
                return ResponseEntity.notFound().build();
            }

            // Get the file path from the URL in the shared link
            Path filePath;
            try {
                URI fileUri = new URI(sharedLink.get().getUrl());
                filePath = Paths.get(fileUri);
            } catch (Exception e) {
                // Fallback to constructed path if URL parsing fails
                filePath = Paths.get("uploads/" + encodedEmail + "/" + downloadName + "/" + downloadName + ".zip");
            }

            if (!Files.exists(filePath)) {
                log.error("File not found at path: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            log.info("Serving file: {}", filePath);
            return serveFile(filePath, downloadName + ".zip");

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