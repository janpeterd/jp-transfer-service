package com.janpeterdhalle.transfer.dtos;

import java.io.Serializable;

import com.janpeterdhalle.transfer.models.Role;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for {@link com.janpeterdhalle.transfer.models.User}
 */
@Data
@Builder
public class UserResponseDto implements Serializable {
    Long id;
    String username;
    String email;
    Role role;
}
