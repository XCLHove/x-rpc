package com.xclhove.rpc.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Random;

/**
 * @author xclhove
 */
@Slf4j
@RequiredArgsConstructor
public class MockServiceProxy implements InvocationHandler {
    private static volatile Random random;
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("mock method: {}::{}", method.getDeclaringClass().getName(), method.getName());
        Class<?> returnType = method.getReturnType();
        return getMockResult(returnType);
    }
    
    private Object getMockResult(Class<?> classType) {
        if (!classType.isPrimitive()) {
            return null;
        }
        if (classType == short.class) {
            return (short) getRandom().nextInt() % Short.MAX_VALUE;
        }
        if (classType == int.class) {
            return getRandom().nextInt();
        }
        if (classType == long.class) {
            return getRandom().nextLong();
        }
        if (classType == float.class) {
            return getRandom().nextFloat();
        }
        if (classType == double.class) {
            return getRandom().nextDouble();
        }
        if (classType == boolean.class) {
            return false;
        }
        if (classType == char.class) {
            char c = 'a';
            return c + getRandom().nextInt() % 26;
        }
        return null;
    }
    
    private Random getRandom() {
        if (random != null) {
            return random;
        }
        synchronized (MockServiceProxy.class) {
            random = new Random();
        }
        return random;
    }
}
