package com.janpeterdhalle.transfer.dtos;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for {@link com.janpeterdhalle.transfer.models.FileEntity}
 */
@Data
@Builder
public class FileEntityRequestDto implements Serializable {
    @NotBlank(message = "fileName can not be blank.")
    String fileName;
    String fileType;
    @NotNull(message = "fileSize must not be null.")
    @Positive(message = "fileSize must be positive.")
    Long fileSize;
    @Size(message = "SHA1 checksum must be 40 long.", min = 40, max = 40)
    @NotBlank(message = "fileChecksum can not be blank.")
    String fileChecksum;
    @NotNull
    @Positive
    private Integer totalChunks;

    @NotNull
    @Positive
    private Integer chunkSize;
}
