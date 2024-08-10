package com.xclhove.rpc.fault.tolerant;

import com.xclhove.rpc.RpcApplication;
import com.xclhove.rpc.spi.SpiLoader;

/**
 * @author xclhove
 */
public final class TolerantStrategyFactory {
    private static final TolerantStrategy DEFAULT = new FailFastTolerantStrategy();
    
    public static TolerantStrategy getInstance() {
        TolerantStrategy instance = SpiLoader.getInstance(TolerantStrategy.class,
                RpcApplication.getConfig().getTolerantStrategyImpl());
        return instance == null ? DEFAULT : instance;
    }
}
