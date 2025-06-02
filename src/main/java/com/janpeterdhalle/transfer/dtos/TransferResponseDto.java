package com.janpeterdhalle.transfer.dtos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.janpeterdhalle.transfer.SharedLinkResponseDto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for {@link com.janpeterdhalle.transfer.models.Transfer}
 */
@Data
@Builder
public class TransferResponseDto implements Serializable {
    Long id;
    LocalDateTime startTime;
    LocalDateTime endTime;
    UserResponseDto user;
    Long totalSize;
    SharedLinkResponseDto sharedLink;
    @Builder.Default
    Set<FileEntityResponseDto> files = new HashSet<>();
}
