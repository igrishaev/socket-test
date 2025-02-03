package me.ivan;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class IOTool {

    public static CompletableFuture<ByteBuffer> readBB(final AsynchronousSocketChannel channel, final int len) {
        final CompletableFuture<ByteBuffer> fut = new CompletableFuture<>();
        final ByteBuffer bb = ByteBuffer.allocate(len);
        channel.read(bb, null, new CompletionHandler<>() {
            @Override
            public void completed(final Integer result, final Object attachment) {
                if (bb.hasRemaining()) {
                    channel.read(bb, null, this);
                } else {
                    fut.complete(bb.rewind());
                }
            }
            @Override
            public void failed(final Throwable e, final Object attachment) {
                fut.completeExceptionally(e);
            }
        });
        return fut;
    }

    public static CompletableFuture<Byte> readByte(final AsynchronousSocketChannel channel) {
        return readBB(channel, 1).thenApplyAsync(ByteBuffer::get);
    }

    public static CompletableFuture<Integer> readInteger(final AsynchronousSocketChannel channel) {
        return readBB(channel, 4).thenApplyAsync(ByteBuffer::getInt);
    }

    public static CompletableFuture<byte[]> readBytes(final AsynchronousSocketChannel channel, final int len) {
        return readBB(channel, len).thenApplyAsync(ByteBuffer::array);
    }

    public static CompletableFuture<byte[]> readByteArray(final AsynchronousSocketChannel channel) {
        return readInteger(channel)
                .thenComposeAsync((final Integer len) -> readBytes(channel, len));
    }

    public static CompletableFuture<String> readString(final AsynchronousSocketChannel channel) {
        return readByteArray(channel)
                .thenApplyAsync((final byte[] buf) -> new String(buf, StandardCharsets.UTF_8));
    }

    public static CompletableFuture<Integer> writeBB(final AsynchronousSocketChannel channel, final ByteBuffer bb) {
        final CompletableFuture<Integer> fut = new CompletableFuture<>();
        bb.rewind();
        channel.write(bb, 0, new CompletionHandler<>() {
            @Override
            public void completed(final Integer read, final Integer sum) {
                if (bb.hasRemaining()) {
                    channel.write(bb, read + sum, this);
                } else {
                    fut.complete(read + sum);
                }
            }
            @Override
            public void failed(final Throwable e, final Integer sum) {
                fut.completeExceptionally(e);
            }
        });
        return fut;
    }

    public static CompletableFuture<Integer> writeInteger (final AsynchronousSocketChannel channel, final int value) {
        final ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(value);
        return writeBB(channel, bb);
    }

    public static CompletableFuture<Integer> writeBytes (final AsynchronousSocketChannel channel, final byte[] buf) {
        final ByteBuffer bb = ByteBuffer.wrap(buf);
        return writeBB(channel, bb);
    }

}
