package me.ivan;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {

    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;

    public void close() {
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public Client(final Socket socket) {
        this.socket = socket;
        try {
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            this.in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public static Client connect(final int port) {
        final Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress("127.0.0.1", port));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new Client(socket);
    }

    public void sendMessage(final String message) throws IOException {
        out.write(message.getBytes(StandardCharsets.UTF_8));
//        final byte[] buf = message.getBytes(StandardCharsets.UTF_8);
//        try {
//            out.writeInt(buf.length);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        try {
//            out.write(buf);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    public String getMessage() throws IOException {
        final byte[] buf = in.readNBytes(4);
        return new String(buf, StandardCharsets.UTF_8);
//        final int len;
//        try {
//            len = in.readInt();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println(String.format("getMessage len: %s", len));
//        final byte[] buf;
//        try {
//            buf = in.readNBytes(len);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return new String(buf, StandardCharsets.UTF_8);
    }



}
