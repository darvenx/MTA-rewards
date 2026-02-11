package com.training.exceptions;

public class SelfTransferException extends RuntimeException{
    public SelfTransferException() {
    }

    public SelfTransferException(String message) {
        super(message);
    }

    public SelfTransferException(String message, Throwable cause) {
        super(message, cause);
    }

    public SelfTransferException(Throwable cause) {
        super(cause);
    }

    public SelfTransferException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
