package me.ivan;

import me.ivan.message.client.*;
import me.ivan.message.server.ErrMessage;
import me.ivan.message.server.OKMessage;
import me.ivan.message.server.ServerMessage;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
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

    public CompletableFuture<ClientMessage> readMessage() {
        return IOTool.readByte(channel)
                .thenComposeAsync((final Byte opcode) -> IOTool.readByteArray(channel)
                        .thenApplyAsync((final byte[] payload) -> {
                            if (opcode == Const.OP_CLIENT_ADD_KEY) {
                                return AddKeyMessage.fromBytes(payload);
                            } else if (opcode == Const.OP_CLIENT_DEL_KEY) {
                                return DelKeyMessage.fromBytes(payload);
                            } else if (opcode == Const.OP_CLIENT_CLEAR) {
                                return ClearMessage.INSTANCE;
                            } else if (opcode == Const.OP_CLIENT_KEY_COUNT) {
                                return CountMessage.INSTANCE;
                            } else {
                                return UnknownMessage.INSTANCE;
                            }
                        }));
    }

    public CompletableFuture<Integer> sendMessage(final ServerMessage message) {
        final ByteBuffer bb = message.toByteBuffer();
        return IOTool.writeBB(channel, bb);
    }

    private ServerMessage handleAddKeyMessage(final AddKeyMessage message) {
        storage.setKey(message.key(), message.val());
        return new OKMessage("key has been added");
    }

    private ServerMessage handleDelKeyMessage(final DelKeyMessage msg) {
        storage.delKey(msg.key());
        return new OKMessage("key has been deleted");
    }

    private ServerMessage handleClearMessage(final ClearMessage msg) {
        storage.clear();
        return new OKMessage("storage has been cleaned");
    }

    private ServerMessage handleCountMessage(final CountMessage msg) {
        final int len = storage.count();
        return new OKMessage("key count: " + len);
    }

    private ServerMessage handleUnknownMessage(final ClientMessage msg) {
        return new ErrMessage("unknown message: " + msg);
    }

    public ServerMessage handleMessage(final ClientMessage msg) {
        if (msg instanceof AddKeyMessage akm) {
            return handleAddKeyMessage(akm);
        } else if (msg instanceof DelKeyMessage dkm) {
            return handleDelKeyMessage(dkm);
        } else if (msg instanceof ClearMessage cm) {
            return handleClearMessage(cm);
        } else if (msg instanceof CountMessage cm) {
            return handleCountMessage(cm);
        } else {
            return handleUnknownMessage(msg);
        }
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
