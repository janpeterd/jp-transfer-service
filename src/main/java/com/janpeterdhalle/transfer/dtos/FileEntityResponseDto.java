package com.janpeterdhalle.transfer.dtos;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for {@link com.janpeterdhalle.transfer.models.FileEntity}
 */
@Data
@Builder
public class FileEntityResponseDto implements Serializable {
    Long id;
    String fileName;
    String filePath;
    String fileType;
    Long fileSize;
    String fileChecksum;
    Integer totalChunks;
}
