package com.janpeterdhalle.transfer.exceptions;


public class FileEntityNotFoundException extends ObjectNotFoundException {
    public FileEntityNotFoundException() {
        super("FileEntity not found");
    }

    public FileEntityNotFoundException(String message) {
        super(message);
    }
}
