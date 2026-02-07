package com.training.exceptions;


// When a transaction is attempted more than once [preventing dupes]
public class IncorrectPinException extends RuntimeException{
    public IncorrectPinException() {
    }

    public IncorrectPinException(String message) {
        super(message);
    }

    public IncorrectPinException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectPinException(Throwable cause) {
        super(cause);
    }

    public IncorrectPinException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
