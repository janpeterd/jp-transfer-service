package com.janpeterdhalle.transfer.exceptions;

import java.time.Instant;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String message, Instant expiresAt) {
        super("Token EXPIRED at " + expiresAt.toString() + ": " + message);
    }
}
