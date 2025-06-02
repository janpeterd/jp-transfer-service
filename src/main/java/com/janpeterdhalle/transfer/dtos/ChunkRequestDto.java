package com.janpeterdhalle.transfer.dtos;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class ChunkRequestDto {
    Integer fileId;
    Integer chunkIndex;
    Integer chunkSize;
    String chunkChecksum;
    MultipartFile multipartFile;
}
