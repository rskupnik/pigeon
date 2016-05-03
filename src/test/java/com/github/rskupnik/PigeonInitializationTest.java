package com.github.rskupnik;

import com.github.rskupnik.exceptions.PigeonException;
import com.github.rskupnik.networking.Server;
import com.github.rskupnik.parrot.Parrot;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PigeonInitializationTest {

    @Mock
    private Parrot parrot;

    private Pigeon pigeon;

    @Before
    public void before() {
        when(parrot.get("port")).thenReturn(Optional.empty());
        when(parrot.get("receiver_mode")).thenReturn(Optional.empty());
        Parrot.mock(parrot);
    }

    @After
    public void after() {
        pigeon.stop();
    }

    @Test
    public void shouldStartWithDefaultPort() throws PigeonException {
        // given
        pigeon = Pigeon.newServer().build();

        // when
        pigeon.start();

        // then
        assertEquals(Defaults.PORT, pigeon.getPort());
    }

    @Test
    public void shouldStartWithConfigFilePort() throws PigeonException {
        // given
        int port = 9293;
        when(parrot.get("port")).thenReturn(Optional.of(String.valueOf(port)));
        pigeon = Pigeon.newServer().build();

        // when
        pigeon.start();

        // then
        assertEquals(port, pigeon.getPort());
    }

    @Test
    public void shouldStartWithPassedPort() throws PigeonException {
        // given
        int configPort = 9293;
        int actualPort = 9294;
        when(parrot.get("port")).thenReturn(Optional.of(String.valueOf(configPort)));
        pigeon = Pigeon.newServer().withPort(actualPort).build();

        // when
        pigeon.start();

        // then
        assertEquals(actualPort, pigeon.getPort());
    }

    @Test
    public void shouldStartWithDefaultReceiverMode() throws PigeonException {
        // given
        pigeon = Pigeon.newServer().build();

        // when
        pigeon.start();

        // then
        assertEquals(Server.ReceiverMode.valueOf(Defaults.RECEIVER_MODE.toUpperCase()), pigeon.getReceiverMode());
    }

    @Test
    public void shouldStartWithConfigFileReceiverMode() throws PigeonException {
        // given
        String mode = "threadpooled";
        when(parrot.get("receiver_mode")).thenReturn(Optional.of(mode));
        pigeon = Pigeon.newServer().build();

        // when
        pigeon.start();

        // then
        assertEquals(Server.ReceiverMode.valueOf(mode.toUpperCase()), pigeon.getReceiverMode());
    }

    @Test
    public void shouldStartWithPassedReceiverMode() throws PigeonException {
        // given
        String invalidMode = "threadpooled";
        String mode = "multithreaded";
        when(parrot.get("receiver_mode")).thenReturn(Optional.of(invalidMode));
        pigeon = Pigeon.newServer().withReceiverMode(mode).build();

        // when
        pigeon.start();

        // then
        assertEquals(Server.ReceiverMode.valueOf(mode.toUpperCase()), pigeon.getReceiverMode());
    }
}
