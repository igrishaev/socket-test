package me.ivan;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread implements AutoCloseable {

    private final ServerSocketChannel channel;
    private final int maxSessions = 4;
    private final List<Session> sessions;
    private boolean isClosed;

    private Server(final ServerSocketChannel channel) {
        this.channel = channel;
        this.sessions = new ArrayList<>(maxSessions);
        this.isClosed = false;
    }

    public static Server start(final int port) {
        final ServerSocketChannel channel;
        try {
            channel = ServerSocketChannel.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            channel.bind(new InetSocketAddress("127.0.0.1", port), 16);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final Server server = new Server(channel);
        server.start();
        return server;
    }

    public void run() {
        while (!isInterrupted()) {
            System.out.println("server run cycle");
            SocketChannel ch;
            try {
                ch = channel.accept();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("new conn");
            final Session session;
            try {
                session = Session.create(s);
                sessions.add(session);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void close() throws Exception {
        this.interrupt();
        for (Session session: sessions) {
            session.interrupt();
        }
        for (Session session: sessions) {
            session.join();
        }
        this.join();
        isClosed = true;
    }
}
