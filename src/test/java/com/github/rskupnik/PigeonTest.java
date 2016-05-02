package com.github.rskupnik;

import com.github.rskupnik.exceptions.PigeonException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class PigeonTest {

    @Test
    public void emptyTest() throws PigeonException {
        Pigeon.newServer().build().start();
    }

    @Test
    @Ignore
    public void shouldStartWithDefaultPort() throws PigeonException {
        // given
        // TODO: requires a mock of Parrot
        Pigeon pigeon = Pigeon.newServer().build();

        // when
        pigeon.start();

        // then
        assertEquals(Defaults.PORT, pigeon.getPort());
    }
}
