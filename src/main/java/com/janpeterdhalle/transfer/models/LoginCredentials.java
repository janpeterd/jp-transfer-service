package com.janpeterdhalle.transfer.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class LoginCredentials {
    private String email;
    private String password;
}
