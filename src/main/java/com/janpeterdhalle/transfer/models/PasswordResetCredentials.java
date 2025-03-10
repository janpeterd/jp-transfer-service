package com.janpeterdhalle.transfer.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PasswordResetCredentials {
    private String password;
    private String newPassword;
}
