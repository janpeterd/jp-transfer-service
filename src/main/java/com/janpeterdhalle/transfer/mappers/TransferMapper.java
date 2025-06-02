package com.janpeterdhalle.transfer.mappers;

import com.janpeterdhalle.transfer.dtos.TransferRequestDto;
import com.janpeterdhalle.transfer.dtos.TransferResponseDto;
import com.janpeterdhalle.transfer.models.FileEntity;
import com.janpeterdhalle.transfer.models.Transfer;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {FileEntityMapper.class, SharedLinkMapper.class})
public interface TransferMapper {
    Transfer toEntity(TransferResponseDto transferResponseDto);

    @Mapping(target = "files", source = "files")
    TransferResponseDto toDto(Transfer transfer);

    @AfterMapping
    default void calculateAndSetTotalSize(Transfer transfer, @MappingTarget TransferResponseDto dto) {
        if (transfer.getFiles() != null && !transfer.getFiles().isEmpty()) {
            long sum = transfer.getFiles().stream()
                               .mapToLong(FileEntity::getFileSize)
                               .sum();
            dto.setTotalSize(sum);
        } else {
            dto.setTotalSize(0L);
        }
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Transfer partialUpdate(TransferResponseDto transferResponseDto, @MappingTarget Transfer transfer);

    @Mapping(target = "files", ignore = true)
    Transfer toEntity(TransferRequestDto transferRequestDto);

    TransferRequestDto toRequestDto(Transfer transfer);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Transfer partialUpdate(TransferRequestDto transferRequestDto, @MappingTarget Transfer transfer);
}
