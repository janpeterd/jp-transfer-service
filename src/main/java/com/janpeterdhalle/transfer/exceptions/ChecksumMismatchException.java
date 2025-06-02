package com.janpeterdhalle.transfer.exceptions;

public class ChecksumMismatchException extends RuntimeException {
    public ChecksumMismatchException(String message) {
        super(message);
    }
}
