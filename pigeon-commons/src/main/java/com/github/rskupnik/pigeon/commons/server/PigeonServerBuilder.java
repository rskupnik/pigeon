package com.github.rskupnik.pigeon.commons.server;

import com.github.rskupnik.pigeon.commons.exceptions.PigeonServerException;

public interface PigeonServerBuilder {
    PigeonServer build() throws PigeonServerException;
}
