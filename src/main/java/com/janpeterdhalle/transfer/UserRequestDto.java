package com.janpeterdhalle.transfer;

import com.janpeterdhalle.transfer.models.Role;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link com.janpeterdhalle.transfer.models.User}
 */
@Value
public class UserRequestDto implements Serializable {
    String username;
    String email;
    Role role;
}
