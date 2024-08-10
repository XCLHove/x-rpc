package com.xclhove.rpc.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import com.xclhove.rpc.factory.SingletonFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xclhove
 */
@Slf4j
public final class SpiLoader {
    private static final Map<String, Map<String, Class<?>>> interfaceMap = new ConcurrentHashMap<>();
    private static final String SYSTEM_SPI_DIR = "META-INF/rpc/system/";
    private static final String CUSTOM_SPI_DIR = "META-INF/rpc/custom/";
    private static final String[] SCAN_DIRS = new String[]{SYSTEM_SPI_DIR, CUSTOM_SPI_DIR};
    
    public static <T> T getInstance(Class<T> interfaceClass, String implClassName) {
        String interfaceClassName = interfaceClass.getName();
        Map<String, Class<?>> implClassMap = interfaceMap.computeIfAbsent(interfaceClassName, key -> load(interfaceClass));
        if (implClassMap.isEmpty()) {
            log.error("SPI：未找到 {} 的实现类", interfaceClassName);
            return null;
        }
        
        if (!implClassMap.containsKey(implClassName)) {
            log.warn("SPI：配置中未找到 {}", implClassName);
            return null;
        }
        
        Class<?> implClass = implClassMap.get(implClassName);
        if (implClass == null) {
            try {
                implClass = Class.forName(implClassName);
                if (!interfaceClass.isAssignableFrom(implClass)) {
                    log.error("SPI：{} 不是接口 {} 的实现类", implClassName, interfaceClassName);
                    implClassMap.remove(implClassName);
                    interfaceMap.put(interfaceClassName, implClassMap);
                    return null;
                }
                log.info("SPI：加载 {} 的实现类 {} 成功", interfaceClassName, implClassName);
                implClassMap.put(implClassName, implClass);
                interfaceMap.put(interfaceClassName, implClassMap);
            } catch (ClassNotFoundException e) {
                log.error("SPI：未找到 {} 的实现类 {}",interfaceClassName, implClassName);
                return null;
            }
        }
        
        return (T) SingletonFactory.getInstance(implClass);
    }
    
    private static Map<String, Class<?>> load(Class<?> interfaceClass) {
        Map<String, Class<?>> implClassMap = new HashMap<>();
        for (String dir : SCAN_DIRS) {
            List<URL> resources = ResourceUtil.getResources(dir + interfaceClass.getName());
            resources.forEach(resource -> {
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String implClassName;
                    while ((implClassName = bufferedReader.readLine()) != null) {
                        if (StrUtil.isBlank(implClassName)) {
                            continue;
                        }
                        implClassMap.put(implClassName, null);
                    }
                } catch (Exception e) {
                    log.error("SPI：加载 {} 失败：", interfaceClass.getName(), e);
                }
            });
        }
        return implClassMap;
    }
}
