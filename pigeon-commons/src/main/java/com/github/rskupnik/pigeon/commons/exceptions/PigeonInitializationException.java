package com.github.rskupnik.pigeon.commons.exceptions;

public class PigeonInitializationException extends PigeonException {

    public PigeonInitializationException(String msg) {
        super(msg);
    }

    public PigeonInitializationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
