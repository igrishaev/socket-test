package me.ivan;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class AsyncSession {

    private final AsynchronousSocketChannel channel;

    private AsyncSession(final AsynchronousSocketChannel channel) {
        this.channel = channel;
    }

    public static AsyncSession create(final AsynchronousSocketChannel channel) {
        return new AsyncSession(channel);
    }

    public CompletableFuture<String> readMessage() {
        return IOTool
                .readInteger(channel)
                .thenComposeAsync((final Integer len) -> IOTool.readBytes(channel, len))
                .thenApplyAsync((final byte[] buf) -> new String(buf, StandardCharsets.UTF_8));
    }

    public CompletableFuture<Integer> sendMessage(final String message) {
        final byte[] buf = message.getBytes(StandardCharsets.UTF_8);
        final int len = buf.length;
        return IOTool
                .writeInteger(channel, len)
                .thenComposeAsync((final Integer ignored) -> IOTool.writeBytes(channel, buf))
                .thenApplyAsync((final Integer ignored) -> 4 + len);
    }

    public String handleMessage(final String message) {
        System.out.println(message);
        if (message.equals("STOP")) {
            try {
                Thread.sleep(10000);
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
                .thenApplyAsync(this::handleMessage)
                .thenComposeAsync(this::sendMessage)
                .thenComposeAsync((final Integer ignored) -> handle());
    }

}
