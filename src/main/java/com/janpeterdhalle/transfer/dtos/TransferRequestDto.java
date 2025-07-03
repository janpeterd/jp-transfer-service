package com.janpeterdhalle.transfer.dtos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for {@link com.janpeterdhalle.transfer.models.Transfer}
 */
@Data
@Builder
public class TransferRequestDto implements Serializable {
    @NotNull(message = "Start time can not be null.")
    @PastOrPresent(message = "Start time must be in the past or present.")
    LocalDateTime startTime;
    @NotNull(message = "files must not be null.")
    @Size(message = "a transfer requires 1 file (max 10000 files).", min = 1, max = 10000)
    @Builder.Default
    Set<FileEntityRequestDto> files = new HashSet<>();
}
