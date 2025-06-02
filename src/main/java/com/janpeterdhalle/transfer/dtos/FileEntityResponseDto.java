package com.janpeterdhalle.transfer.dtos;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

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
    Integer fileSize;
    String fileChecksum;
    Integer totalChunks;
}
