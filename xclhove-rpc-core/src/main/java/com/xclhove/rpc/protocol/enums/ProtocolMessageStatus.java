package com.xclhove.rpc.protocol.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xclhove
 */
@AllArgsConstructor
@Getter
public enum ProtocolMessageStatus {
    OK("OK", 20),
    BAD_REQUEST("badRequest", 40),
    BAD_RESPONSE("badResponse", 50),
    ;
    private final String description;
    private final int value;
    
    public static ProtocolMessageStatus getEnumByValue(int value) {
        for (ProtocolMessageStatus protocolMessageStatus : ProtocolMessageStatus.values()) {
            if (protocolMessageStatus.value == value) {
                return protocolMessageStatus;
            }
        }
        return null;
    }
}
