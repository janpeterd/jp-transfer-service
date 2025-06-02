package com.janpeterdhalle.transfer.exceptions;

public class UserExistsException extends RuntimeException {
    public UserExistsException() {
        super("User exists already");
    }

    public UserExistsException(String message) {
        super(message);
    }
}
