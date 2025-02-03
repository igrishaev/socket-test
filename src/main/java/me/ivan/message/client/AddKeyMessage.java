package me.ivan.message.client;

import me.ivan.BBTool;

import java.nio.ByteBuffer;

public record AddKeyMessage (String key, String val) implements ClientMessage {
    public static AddKeyMessage fromBytes(final byte[] buf) {
        final ByteBuffer bb = ByteBuffer.wrap(buf);
        final String key = BBTool.readString(bb);
        final String val = BBTool.readString(bb);
        return new AddKeyMessage(key, val);
    }
}
