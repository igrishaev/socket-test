package me.ivan.message.client;

import me.ivan.BBTool;

import java.nio.ByteBuffer;

public record DelKeyMessage (String key) implements ClientMessage {
    public static DelKeyMessage fromBytes(final byte[] buf) {
        final ByteBuffer bb = ByteBuffer.wrap(buf);
        final String key = BBTool.readString(bb);
        return new DelKeyMessage(key);
    }
}
