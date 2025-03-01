package com.janpeterdhalle.transfer.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StorageInfo {
    Long totalSpace;
    Long usedSpace;
    Long availableSpace;
}
