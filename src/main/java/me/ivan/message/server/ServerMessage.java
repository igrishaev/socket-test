package me.ivan.message.server;

import java.nio.ByteBuffer;

public interface ServerMessage {
    ByteBuffer toByteBuffer();
}
