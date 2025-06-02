package com.janpeterdhalle.transfer.exceptions;

public class TransferNotFoundException extends ObjectNotFoundException {
    public TransferNotFoundException() {
        super("Transfer not found");
    }

    public TransferNotFoundException(String message) {
        super(message);
    }
}
