package me.ivan;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class AsyncSession {

    private final AsynchronousSocketChannel channel;
    private final Storage storage;

    private final static System.Logger logger =
            System.getLogger(AsyncSession.class.getCanonicalName());

    private AsyncSession(final AsynchronousSocketChannel channel, final Storage storage) {
        this.channel = channel;
        this.storage = storage;
    }

    public static AsyncSession create(final AsynchronousSocketChannel channel, final Storage storage) {
        return new AsyncSession(channel, storage);
    }

    public static String bytesToString (final byte[] buf) {
        return new String(buf, StandardCharsets.UTF_8);
    }

    public CompletableFuture<ClientMessage> readMessage() {
        return IOTool.readByte(channel)
                .thenComposeAsync((final Byte opcode) -> IOTool.readByteArray(channel)
                        .thenApplyAsync((final byte[] payload) ->
                            switch (opcode) {
                                case 0 -> new AddKeyMessage("aaa", "bbb");
                                case 1 -> throw new RuntimeException("aaa");
                                default -> new UnknownMessage();
                            }
                        ));
    }

    public CompletableFuture<Integer> sendMessage(final String message) {
        final byte[] buf = message.getBytes(StandardCharsets.UTF_8);
        final int len = buf.length;
        return IOTool.writeInteger(channel, len)
                .thenComposeAsync((final Integer ignored) -> IOTool.writeBytes(channel, buf))
                .thenApplyAsync((final Integer ignored) -> 4 + len);
    }

    private CompletableFuture<Void> handleAddKeyMessage(final AddKeyMessage message) {
        storage.setKey(message.key(), message.val());
        return 
    }

    public String handleMessage(final ClientMessage message) {
        if (message instanceof AddKeyMessage akm) {
            return "aaa";
        } else {
            return "bbb";
        }
//        try {
//            return String.format("Your message was: %s", message);
//        } catch (Throwable e) {
//            logger.log(Log.ERROR, "error while handling message", e);
//            return String.format("error response: %s", e.getMessage());
//        }
    }

    private Void exceptionally(final Throwable e) {
        logger.log(Log.ERROR, "unhandled exception in a session", e);
        return null;
    }

    public CompletableFuture<Void> handle(final Object ignored) {
        return readMessage()
                .thenApplyAsync(this::handleMessage)
                .thenComposeAsync(this::sendMessage)
                .thenComposeAsync(this::handle)
                .exceptionallyAsync(this::exceptionally);
    }

    public CompletableFuture<Void> handle() {
        return handle(null);
    }

}
