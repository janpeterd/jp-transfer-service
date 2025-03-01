package com.janpeterdhalle.transfer.controllers;

import com.janpeterdhalle.transfer.models.SharedLink;
import com.janpeterdhalle.transfer.services.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("/upload")
@RequiredArgsConstructor
public class FileUploadController {
    private final FileUploadService fileUploadService;

    @PostMapping
    public ResponseEntity<SharedLink> handleFileUpload(
            @RequestParam MultipartFile file,
            @RequestParam String email,
            @RequestParam boolean chunked,
            @RequestParam Integer totalChunks,
            @RequestParam Integer chunkIndex,
            @RequestParam String uploadName,
            @RequestParam String fileName
                                                      ) {
        try {
            System.out.println("EMAIl: " + email);
            System.out.println("CHUNKED: " + chunked);
            System.out.println("TOTAL CHUNKS: " + totalChunks);
            System.out.println("CHUNK INDEX: " + chunkIndex);
            System.out.println("UPLOAD NAME: " + uploadName);
            System.out.println("FILE NAME: " + fileName);
            return ResponseEntity.ok().body(
                    fileUploadService.handleFileUpload(
                            file,
                            email,
                            chunked,
                            totalChunks,
                            chunkIndex,
                            uploadName,
                            fileName
                                                      )
                                           );
        } catch (IOException e) {
            log.error("IO Exception", e);
            return ResponseEntity.internalServerError().body(null);
        } catch (Exception e) {
            log.error("Bad request with exception {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
}
