package com.github.rskupnik.exceptions;

public class PigeonServerException extends PigeonException{

    public PigeonServerException(String msg) {
        super(msg);
    }

    public PigeonServerException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
