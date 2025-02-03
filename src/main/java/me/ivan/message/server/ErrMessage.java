package me.ivan.message.server;

import me.ivan.Const;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public record ErrMessage (String notice) implements ServerMessage {

    @Override
    public ByteBuffer toByteBuffer() {
        final byte[] buf = notice.getBytes(StandardCharsets.UTF_8);
        final ByteBuffer bb = ByteBuffer.allocate(1 + 4 + buf.length);
        bb.put(Const.OP_SERVER_ERR);
        bb.putInt(buf.length);
        bb.put(buf);
        return bb;
    }
}
