package com.github.rskupnik.pigeon.commons;

import com.github.rskupnik.pigeon.commons.exceptions.PigeonException;

public interface PigeonClientBuilder {
    PigeonClient build() throws PigeonException;
}
