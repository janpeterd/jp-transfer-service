package com.janpeterdhalle.transfer.services;

import com.janpeterdhalle.transfer.Utils;
import com.janpeterdhalle.transfer.models.SharedLink;
import com.janpeterdhalle.transfer.repositories.SharedLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {
    private final SharedLinkRepository sharedLinkRepository;

    @Value("${apiurl}")
    private String apiUrl;

    public SharedLink handleFileUpload(
            MultipartFile file,
            String email,
            boolean chunked,
            Integer totalChunks,
            Integer chunkIndex,
            String uploadName,
            String fileName) throws IOException {
        log.info("Received file: {} - Chunked: {} - Chunk {}/{}",
                 file.getOriginalFilename(),
                 chunked,
                 chunkIndex,
                 totalChunks);
        log.info("File name parameter: {}", fileName);

        // Base64 encode email
        String encodedEmail = Base64.getEncoder().encodeToString(email.getBytes());
        log.info("Email: {}", encodedEmail);

        Path uploadsPath = getAndCreateUploadPath(uploadName, encodedEmail);
        Path chunksPath = uploadsPath.resolve("chunks");
        Path finalZipPath = uploadsPath.resolve("transfer.zip");

        // For chunked files, store each chunk separately with filename information
        // fileName parameter contains the original file name
        String safeFileName = sanitizeFileName(fileName);
        String chunkFileName = String.format("%s.chunk.%05d_of_%05d", safeFileName, chunkIndex, totalChunks);
        Path chunkPath = chunksPath.resolve(chunkFileName);

        log.info("Saving chunk at: {}", chunkPath);
        Files.copy(file.getInputStream(), chunkPath, StandardCopyOption.REPLACE_EXISTING);

        // If this is the last chunk of this file, process all its chunks
        if (isLastChunk(chunkIndex, totalChunks)) {
            log.info("Last chunk of '{}' received, assembling file...", fileName);

            // Get all chunks for this specific file
            String filePrefix = safeFileName + ".chunk.";
            List<Path> fileChunks;
            try (var files = Files.list(chunksPath)) {
                fileChunks = files
                        .filter(path -> path.getFileName().toString().startsWith(filePrefix))
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
            log.info("Deleted chunk at: {}", chunksPath);
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);


        // Check for existing link
        Optional<SharedLink> existingLink = sharedLinkRepository.findSharedLinksByFileNameAndOwnerMailBase64(uploadName,
                                                                                                             encodedEmail);

        if (existingLink.isPresent()) {
            SharedLink link = existingLink.get();
            link.setFileSize(Files.exists(finalZipPath) ? Files.size(finalZipPath) : file.getSize());
            link.setExpiresAt(expiresAt);
            link.setUrl(finalZipPath.toUri().toString());
            link.setDownloadLink(apiUrl + "/download?email=" + encodedEmail + "&downloadName=" + uploadName);
            return sharedLinkRepository.save(link);
        }

        SharedLink sharedLink = SharedLink.builder()
                                          .fileName(uploadName)
                                          .fileSize(Files.exists(finalZipPath) ? Files.size(finalZipPath) : file.getSize())
                                          .expiresAt(expiresAt)
                                          .url(finalZipPath.toUri().toString())
                                          .downloadLink(apiUrl + "/download?email=" + encodedEmail + "&downloadName=" + uploadName)
                                          .ownerMailBase64(encodedEmail)
                                          .build();
        return sharedLinkRepository.save(sharedLink);
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
     * Check if this is the last chunk of a chunked upload
     */
    private boolean isLastChunk(Integer chunkIndex, Integer totalChunks) {
        return totalChunks != null && Objects.equals(chunkIndex, totalChunks);
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