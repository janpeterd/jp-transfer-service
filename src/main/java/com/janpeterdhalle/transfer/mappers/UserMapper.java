package com.janpeterdhalle.transfer.mappers;

import com.janpeterdhalle.transfer.UserRequestDto;
import com.janpeterdhalle.transfer.dtos.UserResponseDto;
import com.janpeterdhalle.transfer.models.User;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    UserResponseDto toDto(User user);

    User toEntity(UserRequestDto userRequestDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User partialUpdate(
        UserRequestDto userRequestDto,
        @MappingTarget User user);
}
