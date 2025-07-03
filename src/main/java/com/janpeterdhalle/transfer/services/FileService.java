package com.janpeterdhalle.transfer.services;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.springframework.stereotype.Service;

import com.janpeterdhalle.transfer.Utils;
import com.janpeterdhalle.transfer.exceptions.ChecksumMismatchException;
import com.janpeterdhalle.transfer.models.Chunk;
import com.janpeterdhalle.transfer.models.FileEntity;
import com.janpeterdhalle.transfer.repositories.ChunkRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FileService {
    private final Utils utils;
    private final ChunkRepository chunkRepository;

    public Path assembleFile(FileEntity file) {
        log.info("Assembling file {} (ID: {})", file.getFileName(), file.getId());
        Path filePath = Utils.getUploadPath(file).resolve("file_" + file.getId()); // Potentially use original filename
                                                                                   // for clarity
        Path chunksPath = Utils.getUploadPath(file).resolve("chunks");

        try {
            List<Chunk> chunksFromDb = chunkRepository.findByFileOrderByChunkIndexAsc(file);

            if (chunksFromDb.isEmpty()) {
                log.error("No chunks found in DB for file {} (ID: {})", file.getFileName(), file.getId());
                throw new RuntimeException("No chunks found for file " + file.getFileName());
            }

            log.info("Found {} chunks in DB for file {} (ID: {}). Expected total chunks: {}",
                    chunksFromDb.size(), file.getFileName(), file.getId(), file.getTotalChunks());

            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(filePath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING))) {
                for (Chunk chunk : chunksFromDb) {
                    Path chunkPath = chunksPath.resolve(file.getId() + "_" + chunk.getChunkIndex());
                    log.info("Appending chunk {} (index: {}, path: {}) to assembled file {}",
                            chunk.getId(), chunk.getChunkIndex(), chunkPath.getFileName(), filePath.getFileName());

                    if (!Files.exists(chunkPath)) {
                        log.error("Chunk file not found on disk: {} for file {} (ID: {})", chunkPath,
                                file.getFileName(), file.getId());
                        throw new RuntimeException("Chunk file not found on disk: " + chunkPath);
                    }
                    Files.copy(chunkPath, out);
                }
            }

            // Robust Checksum Validation
            long assembledFileSize = Files.size(filePath);
            log.info("Assembled file {} (ID: {}) on disk. Size: {}. Expected original checksum: {}",
                    file.getFileName(), file.getId(), assembledFileSize, file.getFileChecksum());

            String actualChecksum = utils.calculateFileChecksum(filePath);
            log.info("Calculated checksum of assembled file {}: {}", file.getFileName(), actualChecksum);

            if (!actualChecksum.equals(file.getFileChecksum())) {
                log.error(
                        "CHECKSUM MISMATCH for assembled file {} (ID: {}). Assembled size: {}, Expected checksum: {}, Actual checksum: {}. Deleting corrupt assembled file.",
                        file.getFileName(), file.getId(), assembledFileSize, file.getFileChecksum(), actualChecksum);
                try {
                    Files.deleteIfExists(filePath); // Clean up corrupt file
                } catch (IOException ex) {
                    log.warn("Failed to delete corrupt assembled file: {}", filePath, ex);
                }
                throw new ChecksumMismatchException("Checksum mismatch for assembled file " + file.getFileName() +
                        ". Expected: " + file.getFileChecksum() + ", Got: " + actualChecksum);
            } else {
                log.info("Checksum VERIFIED for assembled file {} (ID: {})", file.getFileName(), file.getId());
            }

        } catch (IOException e) {
            log.error("IOException during assembly of file {} (ID: {}): {}", file.getFileName(), file.getId(),
                    e.getMessage(), e);
            throw new RuntimeException("Error assembling file " + file.getFileName() + ": " + e.getMessage(), e);
        } catch (ChecksumMismatchException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("RuntimeException during assembly of file {} (ID: {}): {}", file.getFileName(), file.getId(),
                    e.getMessage(), e);
            throw e;
        }
        return filePath;
    }
}
