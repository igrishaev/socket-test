package me.ivan;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Session extends Thread {
    private final Socket socket;

    public Session(final Socket socket) {
        this.socket = socket;
    }

    private String readMessage(final DataInputStream in) {
        System.out.println("reading");
        final int len;
        try {
            len = in.readInt();
        } catch (EOFException e) {
            return "";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(len);
        final byte[] buf;
        try {
            buf = in.readNBytes(len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new String(buf, StandardCharsets.UTF_8);
    }

    private static void sendMessage(final DataOutputStream out, final String message) {
        final byte[] buf = message.getBytes(StandardCharsets.UTF_8);
        try {
            out.writeInt(buf.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            out.write(buf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        final DataInputStream in;
        try {
            in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final DataOutputStream out;
        try {
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            final String request = readMessage(in);
            if (request.isEmpty()) {
                return;
            }
            System.out.println(request);
            final String response = String.format("Your message was: %s", request);
            sendMessage(out, response);
        }
    }
}
