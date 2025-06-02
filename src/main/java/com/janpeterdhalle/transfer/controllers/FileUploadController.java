package com.janpeterdhalle.transfer.controllers;

import com.janpeterdhalle.transfer.dtos.ChunkRequestDto;
import com.janpeterdhalle.transfer.services.ChunkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {
    private final ChunkService chunkService;

    @PostMapping
    public void handleFileUpload(@ModelAttribute ChunkRequestDto chunkRequestDto) {
        chunkService.upload(chunkRequestDto);
    }
}
