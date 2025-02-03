package me.ivan;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncServer implements AutoCloseable, CompletionHandler<AsynchronousSocketChannel, Object> {

    private final AsynchronousServerSocketChannel channel;
    private final SocketAddress address;
    private final AtomicBoolean isClosed;
    private final Storage storage;

    private final static System.Logger logger =
            System.getLogger(Server.class.getCanonicalName());

    private AsyncServer(final AsynchronousServerSocketChannel channel,
                        final SocketAddress address) {
        this.channel = channel;
        this.address = address;
        this.isClosed = new AtomicBoolean(false);
        this.storage = Storage.create();
    }

    public static AsyncServer create(final String host,
                                     final int port,
                                     final int backlog) throws IOException {

        final AsynchronousServerSocketChannel channel = AsynchronousServerSocketChannel.open();
        final SocketAddress address = new InetSocketAddress(host, port);
        channel.bind(address, backlog);
        final AsyncServer server = new AsyncServer(channel, address);
        server.loop();
        logger.log(Log.INFO, String.format("server %s has been started", server));
        return server;
    }

    @Override
    public String toString() {
        return String.format("<AsyncServer %s, closed: %s>", address, isClosed.get());
    }

    public void loop() {
        if (!isClosed.get()) {
            channel.accept(null, this);
        }
    }

    @Override
    public void completed(final AsynchronousSocketChannel result, final Object attachment) {
        CompletableFuture.supplyAsync(() -> AsyncSession.create(result, storage).handle());
        loop();
    }

    @Override
    public void failed(final Throwable e, final Object attachment) {
        logger.log(Log.ERROR, String.format("failed to accept a new connection, server: %s", this), e);
        loop();
    }

    @Override
    public void close() throws IOException {
        isClosed.set(true);
        channel.close();
        logger.log(Log.INFO, String.format("server %s has been stopped", this));
    }
}
