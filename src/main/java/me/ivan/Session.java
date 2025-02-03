package me.ivan;

import java.io.*;
import java.net.SocketException;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Session extends Thread implements AutoCloseable, Thread.UncaughtExceptionHandler {

    private final UUID uuid;
    private final SocketChannel socketChannel;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private final TryLock lock;

    private final static System.Logger logger = System.getLogger(Server.class.getCanonicalName());

    private Session(final SocketChannel socketChannel,
                    final DataOutputStream dataOutputStream,
                    final DataInputStream dataInputStream
    ) {
        this.uuid = UUID.randomUUID();
        this.socketChannel = socketChannel;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.lock = TryLock.create();
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof Session s) {
            return this.uuid == s.uuid;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("Session %s, thread id: %s",
                this.uuid,
                threadId()
        );
    }

    public static Session create(final SocketChannel socketChannel) throws IOException {
        final DataInputStream dataInputStream = new DataInputStream(Channels.newInputStream(socketChannel));
        final DataOutputStream dataOutputStream = new DataOutputStream(Channels.newOutputStream(socketChannel));
        final Session session = new Session(socketChannel, dataOutputStream, dataInputStream);
        session.setUncaughtExceptionHandler(session);
        session.start();
        return session;
    }

    private String readMessage() throws IOException {
        final int len = dataInputStream.readInt();
        final byte[] buf = dataInputStream.readNBytes(len);
        return new String(buf, StandardCharsets.UTF_8);
    }

    private void sendMessage(final String message) throws IOException {
        final byte[] buf = message.getBytes(StandardCharsets.UTF_8);
        dataOutputStream.writeInt(buf.length);
        dataOutputStream.write(buf);
    }

    private String handle(final String request) {
        return String.format("Your message was: %s", request);
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                final String request = readMessage();
                final String response = handle(request);
                sendMessage(response);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    public void close() throws Exception {
        interrupt();
        join();
        socketChannel.close();
        dataInputStream.close();
        dataOutputStream.close();

    }

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        final Throwable cause = e.getCause();
        if (cause instanceof EOFException ignored) {
            logger.log(Log.INFO, "Session connection was closed");
        } else if (cause instanceof ClosedByInterruptException ignored) {
            logger.log(Log.INFO, "Session thread was interrupted");
        }
        else {
            logger.log(Log.ERROR, "Server unexpected exception", e);
        }
    }
}
