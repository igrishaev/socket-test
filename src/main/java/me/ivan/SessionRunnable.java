
package me.ivan;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class SessionRunnable implements Runnable, AutoCloseable {

    private final SocketChannel socketChannel;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private final static System.Logger logger = System.getLogger(Server.class.getCanonicalName());

    private SessionRunnable(final SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    private void initiate() {
        this.dataInputStream = new DataInputStream(Channels.newInputStream(socketChannel));
        this.dataOutputStream = new DataOutputStream(Channels.newOutputStream(socketChannel));
    }

    public static SessionRunnable create(final SocketChannel socketChannel) {
        return new SessionRunnable(socketChannel);
    }

    @Override
    public String toString() {
        return String.format("<Session %s>", socketChannel);
    }

    @Override
    public void run() {
        initiate();
        while (true) {
            try {
                interact();
            } catch (IOException e) {
                logger.log(Log.ERROR, "IO error", e);
                break;
            }
        }
        close();
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

    private String handleMessage(final String request) {
        return String.format("Your message was: %s", request);
    }

    private void interact() throws IOException {
        final String request = readMessage();
        String response;
        try {
            response = handleMessage(request);
        } catch (Exception e) {
            logger.log(Log.ERROR, "error while handling a message", e);
            response = String.format("error: %s", e.getMessage());
        }
        sendMessage(response);
    }

    @Override
    public void close() throws Exception {
        dataInputStream.close();
        dataOutputStream.close();
        socketChannel.close();
    }


//    @Override
//    public void run() {
//        while (!isInterrupted()) {
//            try {
//                final String request = readMessage();
//                final String response = handle(request);
//                sendMessage(response);
//            } catch (IOException e) {
//                throw new UncheckedIOException(e);
//            }
//        }
//    }

//    @Override
//    public void close() throws Exception {
//        interrupt();
//        join();
//        socketChannel.close();
//        dataInputStream.close();
//        dataOutputStream.close();
//    }

//    @Override
//    public void uncaughtException(final Thread t, final Throwable e) {
//        final Throwable cause = e.getCause();
//        if (cause instanceof EOFException ignored) {
//            logger.log(Log.INFO, "Session connection was closed");
//        } else if (cause instanceof ClosedByInterruptException ignored) {
//            logger.log(Log.INFO, "Session thread was interrupted");
//        }
//        else {
//            logger.log(Log.ERROR, "Server unexpected exception", e);
//        }
//    }

}