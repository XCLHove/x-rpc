package com.xclhove.rpc.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

/**
 * @author xclhove
 */
public final class ConfigUtil {
    
    public static <T> T loadConfig(Class<T> classType, String prefix, String environment) {
        StringBuilder configFileName = new StringBuilder("rpc-application");
        if (StrUtil.isNotBlank(environment)) {
            configFileName.append("-").append(environment);
        }
        configFileName.append(".properties");
        
        Props props = new Props(configFileName.toString());
        return props.toBean(classType, prefix);
    }
    
    public static <T> T loadConfig(Class<T> classType, String prefix) {
        return loadConfig(classType, prefix, null);
    }
}
