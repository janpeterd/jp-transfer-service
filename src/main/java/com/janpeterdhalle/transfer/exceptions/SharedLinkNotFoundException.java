package com.janpeterdhalle.transfer.exceptions;

public class SharedLinkNotFoundException extends ObjectNotFoundException {
    public SharedLinkNotFoundException() {
        super("SharedLink not found");
    }

    public SharedLinkNotFoundException(String message) {
        super(message);
    }
}
