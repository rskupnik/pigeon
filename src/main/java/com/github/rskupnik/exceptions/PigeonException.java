package com.github.rskupnik.exceptions;

public class PigeonException extends Exception {

    public PigeonException(String msg) {
        super(msg);
    }

    public PigeonException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
