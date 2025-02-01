package me.ivan;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private final ServerSocket socket;

    protected Server(final ServerSocket socket) {
        this.socket = socket;
    }

    public static Thread start2(final int port) {
        Thread t = new Thread() {
            @Override
            public void run() {
                Server.start(port);
            }
        };
        t.start();
        return t;
    }

    public static void start(final int port) {
        final ServerSocket socket;
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (true) {
            Socket s;
            try {
                s = socket.accept();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("new conn");
            final Session session = new Session(s);
            session.setUncaughtExceptionHandler((Thread t, Throwable e) -> {
                e.printStackTrace();
            });
            session.start();
        }

    }





}
