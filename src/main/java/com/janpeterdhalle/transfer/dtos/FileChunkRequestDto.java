package com.janpeterdhalle.transfer.dtos;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileChunkRequestDto {
    @NotNull
    @Positive
    Integer fileId;
    @NotNull(message = "chunkIndex must not be null")
    @PositiveOrZero(message = "chunkIndex must be positive or 0")
    Integer chunkIndex;
    @NotNull(message = "chunkSize must not be null.")
    @Positive(message = "chunkSize must be positive.")
    Integer chunkSize;
    @Size(message = "SHA1 checksum must be 40 long.", min = 40, max = 40)
    @NotBlank(message = "fileChecksum can not be blank.")
    String chunkChecksum;
}
