package me.ivan;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AsyncServer {

    private final AsynchronousServerSocketChannel channel;
    private final Executor executor;

    public AsyncServer() throws IOException {
        channel = AsynchronousServerSocketChannel.open();
        channel.bind(new InetSocketAddress("127.0.0.1", 21998), 16);
        executor = Executors.newFixedThreadPool(8);
    }

    public void loop() {
        channel.accept(null, new CompletionHandler<>() {
            @Override
            public void completed(AsynchronousSocketChannel result, Object attachment) {
                System.out.println("accepted");
                AsyncSession.create(result, executor).handle();
                channel.accept(null, this);
            }
            @Override
            public void failed(Throwable e, Object attachment) {
                e.printStackTrace();
                channel.accept(null, this);
            }
        });
    }

    public CompletableFuture<AsyncSession> accept() {
        final CompletableFuture<AsyncSession> fut = new CompletableFuture<>();
        channel.accept(null, new CompletionHandler<>() {
            @Override
            public void completed(AsynchronousSocketChannel result, Object attachment) {
                fut.complete(AsyncSession.create(result, executor));
            }
            @Override
            public void failed(Throwable exc, Object attachment) {
                fut.completeExceptionally(exc);
            }
        });
        return fut;
    }





}
