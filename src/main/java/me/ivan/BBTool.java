package me.ivan;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BBTool {

    public static String readString(final ByteBuffer bb) {
        final int len = bb.getInt();
        final byte[] buf = new byte[len];
        bb.get(buf);
        return new String(buf, StandardCharsets.UTF_8);
    }

}
