package com.janpeterdhalle.transfer.exceptions;

public class UserNotFoundException extends ObjectNotFoundException {
    public UserNotFoundException() {
        super("User not found");
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
