package com.janpeterdhalle.transfer.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.janpeterdhalle.transfer.Utils;
import com.janpeterdhalle.transfer.dtos.ChunkRequestDto;
import com.janpeterdhalle.transfer.exceptions.ChecksumMismatchException;
import com.janpeterdhalle.transfer.exceptions.FileEntityNotFoundException;
import com.janpeterdhalle.transfer.mappers.ChunkMapper;
import com.janpeterdhalle.transfer.models.Chunk;
import com.janpeterdhalle.transfer.models.FileEntity;
import com.janpeterdhalle.transfer.repositories.ChunkRepository;
import com.janpeterdhalle.transfer.repositories.FileRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChunkService {
    private final ChunkRepository chunkRepository;
    private final FileRepository fileRepository;
    private final ChunkMapper chunkMapper;
    private final Utils utils;

    public void upload(ChunkRequestDto chunkRequestDto) {
        FileEntity file = fileRepository.findById(Long.valueOf(chunkRequestDto.getFileId()))
                .orElseThrow(FileEntityNotFoundException::new);
        Chunk chunk = chunkMapper.toEntity(chunkRequestDto);
        chunk.setFile(file);
        chunkRepository.save(chunk);

        Path uploadFilePath = Utils.getUploadPath(file);
        Path uploadDir = uploadFilePath.resolve("chunks");
        Path uploadPath = uploadDir.resolve(file.getId() + "_" + chunkRequestDto.getChunkIndex());

        // check if checksum matches (chunk didn't change in transit)
        if (!utils.checkChunkChecksumMatches(chunkRequestDto))
            throw new ChecksumMismatchException("Chunk with id " + chunk.getId() + " checksum does not match.");

        // save to disk
        try {
            Files.createDirectories(uploadDir);
            Files.copy(chunkRequestDto.getMultipartFile().getInputStream(),
                    uploadPath,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("IO Error while uploading chunk", e);
        }

        if (Objects.equals(chunkRequestDto.getChunkIndex(), file.getTotalChunks())) {
            file.setUploaded(true);
            fileRepository.save(file);
        }
    }
}
