package com.xclhove.rpc.protocol;

/**
 * @author xclhove
 */
public interface ProtocolConstant {
    /**
     * 消息头长度
     */
    int MESSAGE_HEADER_LENGTH = 20;
    /**
     * 协议魔数
     */
    byte PROTOCOL_MAGIC = 0x1;
    /**
     * 协议版本
     */
    byte PROTOCOL_VERSION = 0x1;
}
