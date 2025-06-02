package com.janpeterdhalle.transfer.exceptions;

public class RefreshTokenNotFoundException extends ObjectNotFoundException {
    public RefreshTokenNotFoundException() {
        super("RefreshToken not found");
    }

    public RefreshTokenNotFoundException(String message) {
        super(message);
    }
}
