package com.janpeterdhalle.transfer.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AuthResponse {
    private String authToken;
    private String refreshToken;
}
