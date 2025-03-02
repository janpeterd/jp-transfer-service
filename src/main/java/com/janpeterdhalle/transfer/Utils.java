package com.janpeterdhalle.transfer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Utils {
    public static void deleteDirectory(File directory) {
        if (!directory.exists()) {
            System.out.println("Directory does not exist: " + directory.getAbsolutePath()); // Log if directory doesn't exist
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
                            System.err.println("Failed to delete file: " + file.getAbsolutePath() + " - " + e.getMessage()); // Log file deletion failure
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
                System.err.println("Failed to delete directory: " + directory.getAbsolutePath() + " - " + e.getMessage()); // Log directory deletion failure
                // Consider if you want to throw an exception or just log and continue
            }
        } else {
            // If it's not a directory, and we are asked to delete it (in initial call), it might be a single file.
            // However, your handleBeforeDelete is passing a directory path, so this case might not be needed there,
            // but it's good to handle for general utility.
            try {
                Files.delete(directory.toPath()); // Try to delete if it's a file (though should be a directory in your use case)
                System.out.println("Deleted file (single): " + directory.getAbsolutePath()); // Log single file deletion
            } catch (IOException e) {
                System.err.println("Failed to delete file (single): " + directory.getAbsolutePath() + " - " + e.getMessage()); // Log single file deletion failure
            }
        }
    }
}