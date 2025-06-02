package com.janpeterdhalle.transfer.mappers;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Value;

import com.janpeterdhalle.transfer.SharedLinkResponseDto;
import com.janpeterdhalle.transfer.models.SharedLink;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {
        TransferMapper.class })
public abstract class SharedLinkMapper {
    @Value("${apiurl}")
    private String apiUrl;

    @Mapping(source = "transferId", target = "transfer.id")
    public abstract SharedLink toEntity(SharedLinkResponseDto sharedLinkResponseDto);

    @InheritInverseConfiguration(name = "toEntity")
    @Mapping(target = "transferId", source = "transfer.id")
    @Mapping(target = "downloadLink", source = "downloadLink", qualifiedByName = "downloadLinkMapping")
    public abstract SharedLinkResponseDto toDto(SharedLink sharedLink);

    @Named("downloadLinkMapping")
    String prependApiUrlDownloadLink(String downloadLink) {
        return apiUrl + downloadLink;
    }

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract SharedLink partialUpdate(
            SharedLinkResponseDto sharedLinkResponseDto,
            @MappingTarget SharedLink sharedLink);
}
