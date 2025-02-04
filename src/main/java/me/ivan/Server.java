package me.ivan;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server extends Thread implements AutoCloseable, Thread.UncaughtExceptionHandler {

    private final ServerSocketChannel channel;
    private final Executor executor;
    private static final int backlog = 4;
    private final SocketAddress address;

    private boolean isClosed;

    private final static System.Logger logger = System.getLogger(Server.class.getCanonicalName());

    private Server(final ServerSocketChannel channel,
                   final Executor executor,
                   final SocketAddress address) {
        this.channel = channel;
        this.executor = executor;
        this.address = address;
        this.isClosed = false;
    }

    public static Server create(final String host, final int port) throws IOException {
        final SocketAddress address = new InetSocketAddress(host, port);
        final ServerSocketChannel channel = ServerSocketChannel.open();
        channel.bind(address, backlog);
        final Executor executor = Executors.newFixedThreadPool(8);
        final Server server = new Server(channel, executor, address);
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
            executor.execute(SessionRunnable.create(ch));
        }
    }

    @Override
    public String toString() {
        return String.format("<Server %s, closed: %s>", address, isClosed);
    }

    @Override
    public void close() throws Exception {
        this.interrupt();
        this.join();
        isClosed = true;
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
