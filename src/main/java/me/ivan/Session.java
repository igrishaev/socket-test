package me.ivan;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Session extends Thread implements AutoCloseable {

    private final UUID id;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;

    private Session(final DataOutputStream dataOutputStream,
                    final DataInputStream dataInputStream
    ) {
        this.id = UUID.randomUUID();
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof Session s) {
            return this.id == s.id;
        } else {
            return false;
        }
    }

    public static Session create(final Socket socket) throws IOException {
        final DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        final Session session = new Session(dataOutputStream, dataInputStream);
        session.start();
        return session;
    }

    private String readMessage() throws IOException {
        System.out.println("reading");
        final int len = dataInputStream.readInt();
        System.out.println(len);
        final byte[] buf = dataInputStream.readNBytes(len);
        return new String(buf, StandardCharsets.UTF_8);
    }

    private void sendMessage(final String message) throws IOException {
        final byte[] buf = message.getBytes(StandardCharsets.UTF_8);
        dataOutputStream.writeInt(buf.length);
        dataOutputStream.write(buf);
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                final String request = readMessage();
                System.out.println(request);
                final String response = String.format("Your message was: %s", request);
                sendMessage(response);
            } catch (EOFException e) {
                System.out.println("client has disconnected");
                break;
            } catch (IOException e) {
                System.out.println("IO exception has happened");
                break;
            }
        }
        try {
            closeIO();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void closeIO() throws IOException {
        dataInputStream.close();
        dataOutputStream.close();
    }

    @Override
    public void close() throws Exception {
        interrupt();
        join();
    }
}
