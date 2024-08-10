package com.xclhove.rpc.protocol;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;

/**
 * @author xclhove
 */
public class TcpBufferHandlerWrapper implements Handler<Buffer> {
    private final RecordParser recordParser;
    
    public TcpBufferHandlerWrapper(Handler<Buffer> bufferHandler) {
        this.recordParser = buildRecordParser(bufferHandler);
    }
    
    private RecordParser buildRecordParser(Handler<Buffer> resultBufferHandler) {
        // 读取固定长度的 header
        RecordParser parser = RecordParser.newFixed(ProtocolConstant.MESSAGE_HEADER_LENGTH);
        
        parser.setOutput(new Handler<>() {
            int nextReadSize = -1;
            Buffer resultBuffer = Buffer.buffer();
            
            @Override
            public void handle(Buffer handeBuffer) {
                if (nextReadSize == -1) {
                    // 为 -1 则根据 header 的长度读取 header
                    resultBuffer.appendBuffer(handeBuffer);
                    
                    // 拿到 header 中的变长内容的长度
                    int serializerClassNameLength = handeBuffer.getInt(12);
                    int bodyLength = handeBuffer.getInt(16);
                    
                    // 等待 header 读取完毕后读取变长内容
                    nextReadSize = serializerClassNameLength + bodyLength;
                    parser.fixedSizeMode(nextReadSize);
                    
                    return;
                }
                
                // 将变长内容写入到 resultBuffer
                resultBuffer.appendBuffer(handeBuffer);
                
                // 返回拼接完整的 resultBuffer
                resultBufferHandler.handle(resultBuffer);
                
                // 重置，继续下一轮的读取
                parser.fixedSizeMode(ProtocolConstant.MESSAGE_HEADER_LENGTH);
                nextReadSize = -1;
                resultBuffer = Buffer.buffer();
            }
        });
        return parser;
    }
    
    @Override
    public void handle(Buffer buffer) {
        recordParser.handle(buffer);
    }
}
