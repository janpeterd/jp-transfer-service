package com.janpeterdhalle.transfer.services;

import com.janpeterdhalle.transfer.Utils;
import com.janpeterdhalle.transfer.models.SharedLink;
import com.janpeterdhalle.transfer.repositories.SharedLinkRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FileUploadService {
    // Redis keys TTL in days (for cleanup of abandoned transfers)
    private static final int REDIS_KEYS_TTL_DAYS = 7;
    private final SharedLinkRepository sharedLinkRepository;
    private final SchedulingService schedulingService;
    private final RedisTemplate<String, Object> redisTemplate;
    @Value("${apiurl}")
    private String apiUrl;

    public Optional<SharedLink> handleFileUpload(
        MultipartFile file,
        String email,
        boolean chunked,
        Integer totalChunks,
        Integer chunkIndex,
        String uploadName,
        String fileName) throws IOException, InterruptedException {
        log.info("Received file: {} - Chunked: {} - Chunk {}/{}",
                 file.getOriginalFilename(),
                 chunked,
                 chunkIndex,
                 totalChunks);
        log.info("File name parameter: {}", fileName);

        // Base64 encode email
        String encodedEmail = Base64.getEncoder().encodeToString(email.getBytes());
        log.info("Email: {}", encodedEmail);

        // Create Redis keys for tracking
        String transferId = generateTransferId(encodedEmail, uploadName);
        String receivedChunksKey = transferId + ":receivedChunks";
        String totalChunksKey = transferId + ":totalChunks";
        String processingKey = transferId + ":processing";
        String sharedLinkCreatedKey = transferId + ":sharedLinkCreated";

        Optional<SharedLink> sharedLinkOptional = sharedLinkRepository.findOneByFileNameAndOwnerMailBase64(fileName,
                                                                                                           encodedEmail);
        // Create or get the SharedLink only for the first chunk or single-chunk uploads
        if (chunkIndex == 1 || totalChunks == 1) {
            // Set a flag in Redis to indicate we're creating a shared link
            redisTemplate.opsForValue().setIfAbsent(sharedLinkCreatedKey, true, REDIS_KEYS_TTL_DAYS, TimeUnit.DAYS);
            sharedLinkOptional = Optional.of(getOrCreateSharedLink(encodedEmail, uploadName));
        }

        Path uploadsPath = getAndCreateUploadPath(uploadName, encodedEmail);
        Path chunksPath = uploadsPath.resolve("chunks");
        Path finalZipPath = uploadsPath.resolve("transfer.zip");

        // For chunked files, store each chunk separately with filename information
        String safeFileName = sanitizeFileName(fileName);
        String chunkFileName = String.format("%s.chunk.%05d_of_%05d", safeFileName, chunkIndex, totalChunks);
        Path chunkPath = chunksPath.resolve(chunkFileName);

        log.info("Saving chunk at: {}", chunkPath);
        Files.copy(file.getInputStream(), chunkPath, StandardCopyOption.REPLACE_EXISTING);

        // Store total chunks count in Redis (set only once)
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(totalChunksKey))) {
            redisTemplate.opsForValue().set(totalChunksKey, totalChunks);
            redisTemplate.expire(totalChunksKey, REDIS_KEYS_TTL_DAYS, TimeUnit.DAYS);
        }

        // Increment received chunks count in Redis
        Long receivedChunksCount = redisTemplate.opsForValue().increment(receivedChunksKey);
        redisTemplate.expire(receivedChunksKey, REDIS_KEYS_TTL_DAYS, TimeUnit.DAYS);

        log.info("Transfer {} progress: {}/{} chunks received",
                 transferId, receivedChunksCount, totalChunks);

        // Check if all chunks have been received and not already processing
        assert receivedChunksCount != null;
        if (receivedChunksCount.equals(totalChunks.longValue()) &&
            !Boolean.TRUE.equals(redisTemplate.hasKey(processingKey))) {

            // Set processing flag to prevent duplicate processing
            redisTemplate.opsForValue().set(processingKey, true);
            redisTemplate.expire(processingKey, REDIS_KEYS_TTL_DAYS, TimeUnit.DAYS);

            sharedLinkOptional = Optional.of(getOrCreateSharedLink(encodedEmail, uploadName));

            // Process asynchronously for multi-chunk uploads
            if (totalChunks > 1) {
                SharedLink finalSharedLink = sharedLinkOptional.get();
                new Thread(() -> {
                    try {
                        assembleZipFromChunks(chunksPath, finalZipPath, uploadsPath, safeFileName);

                        // Update the shared link with final file size
                        log.info("Final zip path size: {}", Files.size(finalZipPath));
                        finalSharedLink.setFileSize(Files.size(finalZipPath));
                        Optional.of(sharedLinkRepository.save(finalSharedLink));

                        // Schedule expiry now that all chunks are received
                        schedulingService.scheduleExpiry(finalSharedLink);

                        // Clean up Redis keys
                        redisTemplate.delete(receivedChunksKey);
                        redisTemplate.delete(totalChunksKey);
                        redisTemplate.delete(processingKey);
                        redisTemplate.delete(sharedLinkCreatedKey);

                        log.info("Final ZIP assembly completed for transfer {}", transferId);
                    } catch (Exception e) {
                        log.error("Error assembling final ZIP for transfer {}", transferId, e);
                        redisTemplate.delete(processingKey); // Remove processing flag to allow retry
                    }
                }).start();
            } else {
                if (sharedLinkOptional.isPresent()) {
                    SharedLink sharedLink = sharedLinkOptional.get();

                    // For single chunk uploads, process synchronously
                    assembleZipFromChunks(chunksPath, finalZipPath, uploadsPath, safeFileName);

                    sharedLink.setFileSize(Files.size(finalZipPath));
                    sharedLinkOptional = Optional.of(sharedLinkRepository.save(sharedLink));

                    // Schedule expiry
                    schedulingService.scheduleExpiry(sharedLink);

                    // Clean up Redis keys
                    redisTemplate.delete(receivedChunksKey);
                    redisTemplate.delete(totalChunksKey);
                    redisTemplate.delete(processingKey);
                    redisTemplate.delete(sharedLinkCreatedKey);

                    log.info("Single chunk ZIP assembly completed for transfer {}", transferId);
                } else {
                    log.info("No shared link found for transfer {}", transferId);
                }
            }
        }

        // Always return the same sharedLink instance
        return sharedLinkOptional;
    }

    public SharedLink getOrCreateSharedLink(String encodedEmail, String uploadName) {
        // Use distributed lock to prevent concurrent creation
        String lockKey = "lock:sharedLink:" + encodedEmail + ":" + uploadName;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 5, TimeUnit.SECONDS);

        try {
            if (Boolean.TRUE.equals(acquired)) {
                // We have the lock, proceed with get or create logic
                // First, check for a SINGLE existing link using the findOne method
                Optional<SharedLink> existingLink = sharedLinkRepository.findSharedLinksByFileNameAndOwnerMailBase64(
                    uploadName, encodedEmail);

                // If a link exists, use it
                if (existingLink.isPresent()) {
                    SharedLink link = existingLink.get();
                    Path path = Paths.get("uploads/" + encodedEmail + "/" + uploadName + "/transfer.zip");
                    link.setExpiresAt(LocalDateTime.now().plusDays(30));
                    link.setUrl(path.toUri().toString());
                    link.setDownloadLink(apiUrl + "/download?email=" + encodedEmail + "&downloadName=" + uploadName);
                    return sharedLinkRepository.save(link);
                }

                // If we need to check for duplicates and clean them up
                List<SharedLink> existingLinks = sharedLinkRepository.findAllByFileNameAndOwnerMailBase64(
                    uploadName, encodedEmail);

                // If links exist, use the first one and clean up any duplicates
                if (!existingLinks.isEmpty()) {
                    if (existingLinks.size() > 1) {
                        log.warn("Found multiple SharedLink records for {}/{}. Keeping only one.",
                                 encodedEmail,
                                 uploadName);
                        SharedLink keepLink = existingLinks.get(0);

                        // Delete all but the first one
                        for (int i = 1; i < existingLinks.size(); i++) {
                            SharedLink linkToDelete = existingLinks.get(i);
                            sharedLinkRepository.delete(linkToDelete);
                        }

                        // Update the link we're keeping
                        Path path = Paths.get("uploads/" + encodedEmail + "/" + uploadName + "/transfer.zip");
                        keepLink.setExpiresAt(LocalDateTime.now().plusDays(30));
                        keepLink.setUrl(path.toUri().toString());
                        keepLink.setDownloadLink(apiUrl + "/download?email=" + encodedEmail + "&downloadName=" + uploadName);
                        return sharedLinkRepository.save(keepLink);
                    } else {
                        // Just one link, update it
                        SharedLink link = existingLinks.get(0);
                        Path path = Paths.get("uploads/" + encodedEmail + "/" + uploadName + "/transfer.zip");
                        link.setExpiresAt(LocalDateTime.now().plusDays(30));
                        link.setUrl(path.toUri().toString());
                        link.setDownloadLink(apiUrl + "/download?email=" + encodedEmail + "&downloadName=" + uploadName);
                        return sharedLinkRepository.save(link);
                    }
                }

                // No links exist, create a new one
                Path path = Paths.get("uploads/" + encodedEmail + "/" + uploadName + "/transfer.zip");
                SharedLink newLink = SharedLink.builder()
                                               .fileName(uploadName)
                                               .fileSize(0L) // Will be updated once file is fully assembled
                                               .expiresAt(LocalDateTime.now().plusDays(30))
                                               .url(path.toUri().toString())
                                               .downloadLink(apiUrl + "/download?email=" + encodedEmail + "&downloadName=" + uploadName)
                                               .ownerMailBase64(encodedEmail)
                                               .build();

                return sharedLinkRepository.save(newLink);
            } else {
                int retryCount = 0;
                int maxRetries = 3;
                long initialWaitMs = 100;

                while (retryCount < maxRetries) {
                    try {
                        // Wait with exponential backoff
                        long waitTimeMs = initialWaitMs * (long) Math.pow(2, retryCount);
                        Thread.sleep(waitTimeMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Thread interrupted while waiting for SharedLink lock");
                        break;
                    }

                    try {
                        // Try to find the existing link
                        Optional<SharedLink> existingLink = sharedLinkRepository.findOneByFileNameAndOwnerMailBase64(
                            uploadName, encodedEmail);

                        if (existingLink.isPresent()) {
                            return existingLink.get();
                        }
                    } catch (Exception e) {
                        log.warn("Error finding SharedLink during retry: {}", e.getMessage());
                    }

                    retryCount++;
                }

                // Last resort: try once more with the list method and take the first one
                try {
                    List<SharedLink> links = sharedLinkRepository.findAllByFileNameAndOwnerMailBase64(
                        uploadName, encodedEmail);
                    if (!links.isEmpty()) {
                        return links.get(0);
                    }
                } catch (Exception e) {
                    log.error("Failed to find SharedLink after all retries", e);
                }

                // If we still can't find it, create one with a unique constraint
                log.warn("Creating SharedLink after lock acquisition failure - potential duplicate");
                Path path = Paths.get("uploads/" + encodedEmail + "/" + uploadName + "/transfer.zip");
                SharedLink fallbackLink = SharedLink.builder()
                                                    .fileName(uploadName)
                                                    .fileSize(0L)
                                                    .expiresAt(LocalDateTime.now().plusDays(30))
                                                    .url(path.toUri().toString())
                                                    .downloadLink(apiUrl + "/download?email=" + encodedEmail + "&downloadName=" + uploadName)
                                                    .ownerMailBase64(encodedEmail)
                                                    .build();

                try {
                    return sharedLinkRepository.save(fallbackLink);
                } catch (Exception e) {
                    log.error("Failed to create fallback SharedLink", e);
                    // One last attempt to get an existing one
                    return sharedLinkRepository.findAllByFileNameAndOwnerMailBase64(
                        uploadName, encodedEmail).stream().findFirst().orElse(fallbackLink);
                }
            }
        } finally {
            if (Boolean.TRUE.equals(acquired)) {
                redisTemplate.delete(lockKey); // Release the lock
            }
        }
    }

    private String generateTransferId(String encodedEmail, String uploadName) {
        return "transfer:" + encodedEmail + ":" + uploadName;
    }

    private void assembleZipFromChunks(Path chunksPath, Path finalZipPath,
                                       Path uploadsPath, String safeFileName) throws IOException {
        // Get all chunks
        List<Path> fileChunks;
        try (var files = Files.list(chunksPath)) {
            // Sort by chunk index for proper assembly
            fileChunks = files
                .sorted()
                .collect(java.util.stream.Collectors.toList());
        }

        // Extract the original file by combining chunks
        Path extractedFilePath = extractFileFromChunks(fileChunks, uploadsPath.resolve(safeFileName));
        log.info("Extracted file: {}", extractedFilePath);

        // Add the extracted file to the final ZIP
        addFileToZip(finalZipPath, extractedFilePath, safeFileName);

        // Clean up the extracted file
        Files.deleteIfExists(extractedFilePath);

        // Delete the chunks folder
        Utils.deleteDirectory(new File(String.valueOf(chunksPath)));
        log.info("Deleted chunks at: {}", chunksPath);
    }

    private Path getAndCreateUploadPath(String uploadName, String encodedEmail) throws IOException {
        // Create directories
        Path uploadsPath = Paths.get("uploads/" + encodedEmail + "/" + uploadName);
        Files.createDirectories(uploadsPath);

        // Create directory for chunks if this is a chunked upload
        Path chunksPath = Paths.get("uploads/" + encodedEmail + "/" + uploadName + "/chunks");
        Files.createDirectories(chunksPath);

        return uploadsPath;
    }

    /**
     * Sanitize filename to prevent path traversal and ensure compatibility
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unnamed_file";
        }
        // Remove path information, replace invalid characters
        return new File(fileName).getName()
                                 .replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /**
     * Extracts a file from its chunks by concatenating them in order
     */
    private Path extractFileFromChunks(List<Path> chunks, Path outputPath) throws IOException {
        log.info("Extracting file from {} chunks to {}", chunks.size(), outputPath);
        // log all chunks
        System.out.println("TOTAL SIZE ALL CHUNKS " + chunks.stream()
                                                            .mapToDouble((chunk) -> (double) new File(chunk.toString()).length() / 1024)
                                                            .sum());
        // Create output directory if it doesn't exist
        Files.createDirectories(outputPath.getParent());

        // Combine all chunks directly
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(outputPath,
                                                                               StandardOpenOption.CREATE,
                                                                               StandardOpenOption.TRUNCATE_EXISTING))) {

            for (Path chunk : chunks) {
                log.info("Processing chunk: {}", chunk.getFileName());
                // Simply copy chunk data to output file without ZIP processing
                Files.copy(chunk, out);
            }
        }

        return outputPath;
    }

    /**
     * Adds a file to the final ZIP archive
     */
    private void addFileToZip(Path zipPath, Path filePath, String entryName) throws IOException {
        log.info("Adding file {} to ZIP as {}", filePath, entryName);

        // Create temporary ZIP file
        Path tempZipPath = Files.createTempFile("tempzip", ".zip");

        try {
            boolean zipExists = Files.exists(zipPath);

            // Create the ZipOutputStream for the temp file
            try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(tempZipPath))) {

                // If the ZIP exists, copy existing entries
                if (zipExists) {
                    try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(zipPath))) {
                        ZipEntry entry;
                        while ((entry = zipIn.getNextEntry()) != null) {
                            // Skip if entry with same name already exists
                            if (entry.getName().equals(entryName)) {
                                log.info("Entry {} already exists, replacing it", entryName);
                                zipIn.closeEntry();
                                continue;
                            }

                            // Copy entry to new ZIP
                            zipOut.putNextEntry(new ZipEntry(entry.getName()));
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = zipIn.read(buffer)) != -1) {
                                zipOut.write(buffer, 0, bytesRead);
                            }
                            zipOut.closeEntry();
                            zipIn.closeEntry();
                        }
                    }
                }

                // Add the new file to the ZIP
                zipOut.putNextEntry(new ZipEntry(entryName));
                Files.copy(filePath, zipOut);
                zipOut.closeEntry();
            }

            // Replace the original ZIP with the temp file
            Files.move(tempZipPath, zipPath, StandardCopyOption.REPLACE_EXISTING);

        } finally {
            Files.deleteIfExists(tempZipPath);
        }
    }
}
