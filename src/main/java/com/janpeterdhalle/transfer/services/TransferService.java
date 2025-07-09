package com.janpeterdhalle.transfer.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.janpeterdhalle.transfer.Constants;
import com.janpeterdhalle.transfer.Utils;
import com.janpeterdhalle.transfer.dtos.TransferRequestDto;
import com.janpeterdhalle.transfer.dtos.TransferResponseDto;
import com.janpeterdhalle.transfer.exceptions.SharedLinkNotFoundException;
import com.janpeterdhalle.transfer.exceptions.TransferNotFoundException;
import com.janpeterdhalle.transfer.mappers.FileEntityMapper;
import com.janpeterdhalle.transfer.mappers.TransferMapper;
import com.janpeterdhalle.transfer.models.FileEntity;
import com.janpeterdhalle.transfer.models.Transfer;
import com.janpeterdhalle.transfer.repositories.FileRepository;
import com.janpeterdhalle.transfer.repositories.SharedLinkRepository;
import com.janpeterdhalle.transfer.repositories.TransferRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TransferService {
    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;
    private final UserService userService;
    private final FileService fileService;
    private final SharedLinkService sharedLinkService;
    @Qualifier("fileEntityMapper")
    private final FileEntityMapper fileMapper;
    private final FileRepository fileRepository;
    private final SharedLinkRepository sharedLinkRepository;

    public TransferResponseDto start(Authentication authentication, TransferRequestDto transferRequestDto) {
        Transfer transfer = transferMapper.toEntity(transferRequestDto);
        transfer.setUser(userService.getLoggedInUser(authentication));
        transfer.setActive(true);
        transfer = transferRepository.save(transfer);

        Transfer finalTransfer = transfer;
        transferRequestDto.getFiles().forEach(fileRequest -> {
            log.info("File request {}", fileRequest);
            FileEntity file = fileMapper.toEntity(fileRequest);
            file.setTransfer(finalTransfer);
            file.setUser(userService.getLoggedInUser(authentication));
            finalTransfer.getFiles().add(file);
            fileRepository.save(file);
        });

        return transferMapper.toDto(finalTransfer);
    }

    public Path assembleZip(Transfer transfer) {
        Path zipPath = Utils.getUploadPath(transfer).resolve("upload.zip");
        Path tempZipPath = Utils.getUploadPath(transfer).resolve("temp.zip");
        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(tempZipPath))) {
            transfer.getFiles().forEach(file -> {
                // 1. assemble file
                Path filePath = fileService.assembleFile(file);
                try {
                    zipOut.putNextEntry(new ZipEntry(file.getFileName()));
                    // 2. copy file into temporary zip
                    Files.copy(filePath, zipOut);
                    zipOut.closeEntry();
                    // 3. delete the file
                    Files.delete(filePath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 4. replace zip with temp
        try {
            Files.move(tempZipPath, zipPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to replace final zip with temp zip, error" + e);
        }
        return zipPath;
    }

    public TransferResponseDto finishById(Long id) {
        Transfer transfer = transferRepository.findById(id).orElseThrow(TransferNotFoundException::new);
        return finish(transfer);
    }

    public TransferResponseDto finish(Transfer transfer) {
        Path zipPath = assembleZip(transfer);
        transfer.setUploadPath(String.valueOf(zipPath));
        sharedLinkService.create(transfer);
        transfer.setEndTime(LocalDateTime.now());
        transferRepository.save(transfer);
        return transferMapper.toDto(transfer);
    }

    public TransferResponseDto getTransferById(Long id) {
        return transferMapper.toDto(transferRepository.findById(id).orElseThrow(TransferNotFoundException::new));
    }

    public TransferResponseDto getTransferByLinkUuid(String linkUuid) {
        return transferMapper.toDto(transferRepository
                .findBySharedLinkAndActiveTrue(sharedLinkRepository.findSharedLinkByUuid(
                        linkUuid).orElseThrow(
                                SharedLinkNotFoundException::new))
                .orElseThrow(TransferNotFoundException::new));
    }

    public List<TransferResponseDto> getUserTransfers(Authentication authentication) {
        return transferRepository.findAllByUserAndActiveTrue(userService.getLoggedInUser(authentication))
                .stream()
                .map(transferMapper::toDto)
                .toList();
    }

    public void deleteTransferById(Long id) {
        Transfer transfer = transferRepository.findById(id).orElseThrow(TransferNotFoundException::new);
        File dirToDelete = new File(Utils.getUploadPath(transfer).toString());
        if (StringUtils.hasLength(dirToDelete.getAbsolutePath()) && dirToDelete.exists() && dirToDelete.isDirectory()
                && !Constants.forbiddenDeleteDirs.contains(
                        dirToDelete.getAbsolutePath())) {
            Utils.deleteDirectory(dirToDelete);
        } else {
            log.error("Delete transfer failed invalid file/dir: {}", dirToDelete.getAbsolutePath());
        }
        transfer.setActive(false);
        transferRepository.save(transfer);
    }
}
