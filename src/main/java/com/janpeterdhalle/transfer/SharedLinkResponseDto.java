package com.janpeterdhalle.transfer;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Value;

/**
 * DTO for {@link com.janpeterdhalle.transfer.models.SharedLink}
 */
@Value
public class SharedLinkResponseDto implements Serializable {
    Long id;
    String uuid;
    String url;
    String downloadLink;
    Long transferId;
    Long createdAt;
    Long updatedAt;
    LocalDateTime expiresAt;
    Integer downloads;
    Integer maxDownloads;
}
