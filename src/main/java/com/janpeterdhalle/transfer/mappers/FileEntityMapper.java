package com.janpeterdhalle.transfer.mappers;

import com.janpeterdhalle.transfer.dtos.FileEntityRequestDto;
import com.janpeterdhalle.transfer.dtos.FileEntityResponseDto;
import com.janpeterdhalle.transfer.models.FileEntity;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface FileEntityMapper {
    FileEntity toEntity(FileEntityResponseDto fileEntityResponseDto);

    FileEntity toEntity(FileEntityRequestDto fileEntityResponseDto);

    FileEntityResponseDto toDto(FileEntity fileEntity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    FileEntity partialUpdate(
        FileEntityResponseDto fileEntityResponseDto,
        @MappingTarget FileEntity fileEntity);
}
