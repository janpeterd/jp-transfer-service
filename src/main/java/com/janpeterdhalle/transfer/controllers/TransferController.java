package com.janpeterdhalle.transfer.controllers;

import com.janpeterdhalle.transfer.dtos.TransferRequestDto;
import com.janpeterdhalle.transfer.dtos.TransferResponseDto;
import com.janpeterdhalle.transfer.services.SecurityService;
import com.janpeterdhalle.transfer.services.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;
    private final SecurityService securityService;

    @GetMapping
    public List<TransferResponseDto> getUserTransfers(Authentication authentication) {
        return transferService.getUserTransfers(authentication);
    }

    @GetMapping("/{id}")
    @PreAuthorize("#securityService.isOwnerOfTransfer(id, authentication)")
    public TransferResponseDto getTransfer(Authentication authentication, @PathVariable Long id) {
        return transferService.getTransferById(id);
    }

    @GetMapping("/uuid/{linkUuid}")
    public TransferResponseDto getTransfer(@PathVariable String linkUuid) {
        return transferService.getTransferByLinkUuid(linkUuid);
    }


    @PostMapping
    public TransferResponseDto handleStartTransfer(Authentication authentication,
                                                   @RequestBody TransferRequestDto requestDto) {
        log.info("Handling start transfer request: {}", requestDto);
        return transferService.start(authentication, requestDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("#securityService.isOwnerOfTransfer(id, authentication)")
    public void deleteTransfer(Authentication authentication, @PathVariable Long id) {
        transferService.deleteTransferById(id);
    }


    @PostMapping("/{id}/finish")
    @PreAuthorize("#securityService.isOwnerOfTransfer(id, authentication)")
    public TransferResponseDto finishTransfer(Authentication authentication, @PathVariable Long id) {
        return transferService.finishById(id);
    }
}
