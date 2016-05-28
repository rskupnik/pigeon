package com.github.rskupnik.pigeon.commons.client;

import com.github.rskupnik.pigeon.commons.exceptions.PigeonException;

public interface PigeonClientBuilder {
    PigeonClient build() throws PigeonException;
}
