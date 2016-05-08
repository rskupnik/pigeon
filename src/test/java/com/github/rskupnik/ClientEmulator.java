package com.github.rskupnik;

import com.github.rskupnik.exceptions.PigeonException;
import com.github.rskupnik.networking.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientEmulator {

    private Socket socket;

    public ClientEmulator() throws IOException {
        try {
            socket = new Socket("localhost", 9434);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.write(5);

            while (true) {

            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    public static void main(String[] args) throws IOException {
        new ClientEmulator();
    }
}

