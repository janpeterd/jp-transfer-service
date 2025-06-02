package com.janpeterdhalle.transfer.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.janpeterdhalle.transfer.models.StorageInfo;
import com.janpeterdhalle.transfer.services.StorageService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/storage")
@RequiredArgsConstructor
public class StorageController {
    private final StorageService storageService;

    @GetMapping("/info")
    public ResponseEntity<StorageInfo> storage() {
        return ResponseEntity.ok().body(storageService.getStorageInfo());
    }

    @GetMapping("/info/current-user")
    public ResponseEntity<Long> storageUser(Authentication authentication) {
        return ResponseEntity.ok().body(storageService.getUserStorageInfo(authentication));
    }
}
