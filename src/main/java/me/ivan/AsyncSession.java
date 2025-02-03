package me.ivan;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AsyncSession {

    private final AsynchronousSocketChannel channel;
    private final Executor executor;

    private AsyncSession(final AsynchronousSocketChannel channel, final Executor executor) {
        this.channel = channel;
        this.executor = executor;
    }

    public static AsyncSession create(final AsynchronousSocketChannel channel,
                                      final Executor executor) {
        return new AsyncSession(channel, executor);
    }

    public static String bytesToString (final byte[] buf) {
        return new String(buf, StandardCharsets.UTF_8);
    }

    public CompletableFuture<String> readMessage() {
        return IOTool.readInteger(channel)
                .thenComposeAsync((final Integer len) -> IOTool.readBytes(channel, len), executor)
                .thenApplyAsync(AsyncSession::bytesToString, executor);
    }

    public CompletableFuture<Integer> sendMessage(final String message) {
        final byte[] buf = message.getBytes(StandardCharsets.UTF_8);
        final int len = buf.length;
        return IOTool.writeInteger(channel, len)
                .thenComposeAsync((final Integer ignored) -> IOTool.writeBytes(channel, buf), executor)
                .thenApplyAsync((final Integer ignored) -> 4 + len, executor);
    }

    public String handleMessage(final String message) {
        System.out.println(message);
        if (message.equals("STOP")) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return String.format("Your message was: %s", message);
        } catch (Throwable e) {
            return String.format("Error: %s", e.getMessage());
        }
    }

    public CompletableFuture<Integer> handle() {
        return readMessage()
                .thenApplyAsync(this::handleMessage, executor)
                .thenComposeAsync(this::sendMessage, executor)
                .thenComposeAsync((final Integer ignored) -> handle(), executor);
    }

}
