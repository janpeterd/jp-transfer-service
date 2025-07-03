package com.janpeterdhalle.transfer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Component;

import com.janpeterdhalle.transfer.dtos.ChunkRequestDto;
import com.janpeterdhalle.transfer.models.FileEntity;
import com.janpeterdhalle.transfer.models.Transfer;
import com.janpeterdhalle.transfer.models.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class Utils {
    public static Path getUploadPath(User user) {
        return Path.of("uploads/user_" + user.getId());
    }

    public static Path getUploadPath(Transfer transfer) {
        return Path.of("uploads/user_" + transfer.getUser().getId() + "/transfer_" + transfer.getId());
    }

    public static Path getUploadPath(FileEntity file) {
        Path parentPath = getUploadPath(file.getTransfer());
        return parentPath.resolve("file_" + file.getId());
    }

    public Boolean areTransferFilesUploaded(Transfer transfer) {
        return transfer.getFiles().stream().allMatch(FileEntity::getUploaded);
    }

    public Boolean checkChunkChecksumMatches(ChunkRequestDto chunkRequestDto) {
        try {
            return checkChunkChecksumMatches(chunkRequestDto.getMultipartFile().getBytes(),
                    chunkRequestDto.getChunkChecksum());
        } catch (IOException e) {
            throw new RuntimeException("Error reading bytes MultipartFile: " + e);
        }
    }

    public Boolean checkChunkChecksumMatches(byte[] bytes, String checksum) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = messageDigest.digest(bytes);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().equals(checksum);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteDirectory(File directory) {
        if (!directory.exists()) {
            System.out.println("Directory does not exist: " + directory.getAbsolutePath()); // Log if directory doesn't
                                                                                            // exist
            return; // Exit if directory doesn't exist
        }

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file); // Recursive call for subdirectories
                    } else {
                        try {
                            Files.delete(file.toPath()); // Delete files
                            System.out.println("Deleted file: " + file.getAbsolutePath()); // Log file deletion
                        } catch (IOException e) {
                            System.err.println(
                                    "Failed to delete file: " + file.getAbsolutePath() + " - " + e.getMessage()); // Log
                                                                                                                  // file
                                                                                                                  // deletion
                                                                                                                  // failure
                            // Consider if you want to throw an exception or just log and continue
                        }
                    }
                }
            }

            // Now the directory should be empty (after deleting files and subdirectories)
            try {
                Files.delete(directory.toPath()); // Delete the empty directory
                System.out.println("Deleted directory: " + directory.getAbsolutePath()); // Log directory deletion
            } catch (IOException e) {
                System.err
                        .println("Failed to delete directory: " + directory.getAbsolutePath() + " - " + e.getMessage()); // Log
                                                                                                                         // directory
                                                                                                                         // deletion
                                                                                                                         // failure
                // Consider if you want to throw an exception or just log and continue
            }
        } else {
            // If it's not a directory, and we are asked to delete it (in initial call), it
            // might be a single file.
            // However, your handleBeforeDelete is passing a directory path, so this case
            // might not be needed there,
            // but it's good to handle for general utility.
            try {
                Files.delete(directory.toPath()); // Try to delete if it's a file (though should be a directory in your
                                                  // use case)
                System.out.println("Deleted file (single): " + directory.getAbsolutePath()); // Log single file deletion
            } catch (IOException e) {
                System.err.println(
                        "Failed to delete file (single): " + directory.getAbsolutePath() + " - " + e.getMessage()); // Log
                                                                                                                    // single
                                                                                                                    // file
                                                                                                                    // deletion
                                                                                                                    // failure
            }
        }
    }

    public String calculateFileChecksum(Path filePath) throws IOException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm for checksum not found", e);
        }

        try (InputStream fis = Files.newInputStream(filePath);
                DigestInputStream dis = new DigestInputStream(fis, md)) {
            byte[] buffer = new byte[8192]; // 8KB buffer
            // noinspection StatementWithEmptyBody
            while (dis.read(buffer) != -1) {
                // Read operations update the message digest
            }
        }
        byte[] digest = md.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

}
