package com.janpeterdhalle.transfer;

import java.io.Serializable;

import com.janpeterdhalle.transfer.models.Role;

import lombok.Value;

/**
 * DTO for {@link com.janpeterdhalle.transfer.models.User}
 */
@Value
public class UserRequestDto implements Serializable {
    String username;
    String email;
    Role role;
}
