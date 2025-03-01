package com.janpeterdhalle.transfer.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AuthResponse {
    private String token;
    private String email;
    private Role role;
    private boolean valid;
}
