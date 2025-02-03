package me.ivan;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncServer implements AutoCloseable {

    private final AsynchronousServerSocketChannel channel;
    private final SocketAddress address;
    private final AtomicBoolean isClosed;

    private final static System.Logger logger =
            System.getLogger(Server.class.getCanonicalName());

    private AsyncServer(final AsynchronousServerSocketChannel channel,
                        final SocketAddress address) {
        this.channel = channel;
        this.address = address;
        this.isClosed = new AtomicBoolean(false);
    }

    public static AsyncServer create(final String host,
                                     final int port,
                                     final int backlog) throws IOException {

        final AsynchronousServerSocketChannel channel = AsynchronousServerSocketChannel.open();
        final SocketAddress address = new InetSocketAddress(host, port);
        channel.bind(address, backlog);
        final AsyncServer server = new AsyncServer(channel, address);
        server.loop();
        return server;
    }

    @Override
    public String toString() {
        return String.format("<AsyncServer %s, closed: %s>", address, isClosed.get());
    }

    public void loop() {
        channel.accept(null, new CompletionHandler<>() {
            @Override
            public void completed(AsynchronousSocketChannel result, Object attachment) {
                CompletableFuture.supplyAsync(() -> AsyncSession.create(result).handle());
                if (!isClosed.get()) {
                    channel.accept(null, this);
                }
            }
            @Override
            public void failed(Throwable e, Object attachment) {
                logger.log(Log.ERROR, "failed to accept a new connection", e);
                if (!isClosed.get()) {
                    channel.accept(null, this);
                }
            }
        });
    }

    @Override
    public void close() {
        isClosed.set(true);
    }
}
