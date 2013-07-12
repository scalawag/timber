package org.slf4j.impl;

import org.scalawag.timber.bridge.slf4j.Slf4jBridgeMDCAdapter;
import org.slf4j.spi.MDCAdapter;

public class StaticMDCBinder {
    public static final StaticMDCBinder SINGLETON = new StaticMDCBinder();

    private StaticMDCBinder() {}

    public MDCAdapter getMDCA() {
        return new Slf4jBridgeMDCAdapter();
    }

    public String getMDCAdapterClassStr() {
        return Slf4jBridgeMDCAdapter.class.getName();
    }
}
