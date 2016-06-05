package com.github.rskupnik.pigeon.commons.server;

import com.github.rskupnik.pigeon.commons.exceptions.PigeonException;

public interface PigeonServerBuilder {
    PigeonServer build() throws PigeonException;
}
