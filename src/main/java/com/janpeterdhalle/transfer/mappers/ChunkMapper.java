package com.janpeterdhalle.transfer.mappers;

import com.janpeterdhalle.transfer.dtos.ChunkRequestDto;
import com.janpeterdhalle.transfer.models.Chunk;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChunkMapper {
    Chunk toEntity(ChunkRequestDto chunkRequestDto);

    ChunkRequestDto toRequestDto(Chunk chunk);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Chunk partialUpdate(
        ChunkRequestDto chunkRequestDto,
        @MappingTarget Chunk chunk);
}
