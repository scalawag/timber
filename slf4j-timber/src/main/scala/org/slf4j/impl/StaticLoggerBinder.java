package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;
import org.scalawag.timber.bridge.slf4j.*;

public class StaticLoggerBinder implements LoggerFactoryBinder {
    private static final StaticLoggerBinder singleton = new StaticLoggerBinder();

    public static StaticLoggerBinder getSingleton() {
        return singleton;
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return Slf4jBridgeLoggerFactory$.MODULE$;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return "org.scalawag.timber.slf4j.Slf4jLoggerFactory";
    }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
