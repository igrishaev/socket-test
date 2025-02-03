package me.ivan;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread implements AutoCloseable, Thread.UncaughtExceptionHandler {

    private final ServerSocketChannel channel;
    private static final int maxSessions = 4;
    private static final String host = "127.0.0.1";
    private final List<Session> sessions;
    private boolean isClosed;
    private final TryLock lock;

    private final static System.Logger logger = System.getLogger(Server.class.getCanonicalName());

    private Server(final ServerSocketChannel channel) {
        this.channel = channel;
        this.sessions = new ArrayList<>(maxSessions);
        this.isClosed = false;
        this.lock = TryLock.create();
    }

    public static Server create(final int port) throws IOException {
        final ServerSocketChannel channel = ServerSocketChannel.open();
        channel.bind(new InetSocketAddress(host, port), maxSessions);
        final Server server = new Server(channel);
        server.setUncaughtExceptionHandler(server);
        server.start();
        return server;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            SocketChannel ch;
            try {
                ch = channel.accept();
            } catch (ClosedByInterruptException e) {
                break;
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            final Session session;
            try {
                session = Session.create(ch);
                try (TryLock ignored = lock.get()) {
                    sessions.add(session);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("unused")
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void close() throws Exception {
        try (TryLock ignored = lock.get()) {
            this.interrupt();
            for (Session session: sessions) {
                session.interrupt();
            }
            for (Session session: sessions) {
                session.join();
            }
            for (Session session: sessions) {
                session.close();
            }
            this.join();
            isClosed = true;
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        final Throwable cause = e.getCause();
        if (cause instanceof ClosedByInterruptException ignored) {
            logger.log(Log.INFO, "Server main loop was interrupted");
        } else {
            logger.log(Log.ERROR, "Session unexpected exception", e);
        }
    }
}
