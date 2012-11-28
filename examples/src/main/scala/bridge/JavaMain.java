package bridge;

import org.slf4j.*;

public class JavaMain {
    public static void main(String[] args) {
        // If you remove this call to Logging.configurate(), the slf4j bridge will use the built-in LoggerManager
        // from the org.scalawag.timber.api.slf4j package.
        Logging.configurate();
        Logger log = LoggerFactory.getLogger("ABC");
        log.debug("Hello, world!");
    }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
