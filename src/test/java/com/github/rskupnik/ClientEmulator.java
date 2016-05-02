package com.github.rskupnik;

import com.github.rskupnik.exceptions.PigeonException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientEmulator {

    private Socket socket;

    public ClientEmulator() throws IOException {
        try {
            Pigeon.newServer().withPort(9432).build().start();

            socket = new Socket("localhost", 9432);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.write(1);
            while (true) {

            }
        } catch (UnknownHostException | PigeonException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    public static void main(String[] args) throws IOException {
        new ClientEmulator();
    }
}

