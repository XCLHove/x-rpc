package com.xclhove.rpc.protocol;

import io.vertx.core.buffer.Buffer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author xclhove
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Accessors(chain = true)
public class BufferReader {
    private int offset = 0;
    private final Buffer buffer;
    
    public byte readByte() {
        byte byteData = buffer.getByte(offset);
        offset += 1;
        return byteData;
    }
    
    public byte[] readBytes(int length) {
        byte[] bytesData = buffer.getBytes(offset, offset + length);
        offset += length;
        return bytesData;
    }
    
    public short readShort() {
        short shortData = buffer.getShort(offset);
        offset += 2;
        return shortData;
    }
    
    public int readInt() {
        int intData = buffer.getInt(offset);
        offset += 4;
        return intData;
    }
    
    public long readLong() {
        long longData = buffer.getLong(offset);
        offset += 8;
        return longData;
    }
    
    public String readString(int length) {
        String stringData = buffer.getString(offset, offset + length);
        offset += length;
        return stringData;
    }
}
