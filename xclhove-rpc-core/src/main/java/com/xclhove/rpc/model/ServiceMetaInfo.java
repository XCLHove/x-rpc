package com.xclhove.rpc.model;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xclhove.rpc.constant.RpcConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author xclhove
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ServiceMetaInfo implements Serializable {
    private String name;
    private String version = RpcConstant.SERVICE_DEFAULT_VERSION;
    private String host;
    private Integer port;
    
    @JsonIgnore
    public String getServiceKey() {
        return String.format("%s:%s", name, version);
    }
    
    @JsonIgnore
    public String getNodeKey() {
        return String.format("%s:%s", host, port);
    }
}
