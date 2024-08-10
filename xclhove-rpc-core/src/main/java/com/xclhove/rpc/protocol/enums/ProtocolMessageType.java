package com.xclhove.rpc.protocol.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xclhove
 */
@AllArgsConstructor
@Getter
public enum ProtocolMessageType {
    UNKNOWN(-1),
    REQUEST(0),
    RESPONSE(1),
    HEART_BEAT(2),
    ;
    
    private final int value;
    
    public static ProtocolMessageType getEnumByValue(int value) {
        for (ProtocolMessageType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }
}
