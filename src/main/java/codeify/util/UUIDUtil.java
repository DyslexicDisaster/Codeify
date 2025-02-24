package codeify.util;

import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.util.UUID;

public final class UUIDUtil {

    private UUIDUtil() {
    }

    public static UUID fromHex(String uuid) throws Exception {
        byte[] data = Hex.decodeHex(uuid.toCharArray());
        ByteBuffer buffer = ByteBuffer.wrap(data);
        return new UUID(buffer.getLong(), buffer.getLong());
    }

    public static String toHex(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return Hex.encodeHexString(buffer.array());
    }
}