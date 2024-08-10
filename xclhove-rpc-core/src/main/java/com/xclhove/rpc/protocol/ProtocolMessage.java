package com.xclhove.rpc.protocol;

import cn.hutool.core.util.IdUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author xclhove
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ProtocolMessage<T> {
    /**
     * 消息头
     */
    private Header header;
    /**
     * 序列化器类名
     */
    private String serializerClassName;
    /**
     * 消息体
     */
    private T body;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Header {
        /**
         * 魔数，保证安全性
         */
        private byte magic = ProtocolConstant.PROTOCOL_MAGIC;
        /**
         * 版本
         */
        private byte version = ProtocolConstant.PROTOCOL_VERSION;
        /**
         * 消息类型（request/response），默认 request
         */
        private byte type;
        /**
         * 状态
         */
        private byte status;
        /**
         * 请求 id
         */
        private long requestId = IdUtil.getSnowflakeNextId();
        /**
         * 序列化器名长度
         */
        private int serializerClassNameLength;
        /**
         * 消息体长度
         */
        private int bodyLength;
    }
}
