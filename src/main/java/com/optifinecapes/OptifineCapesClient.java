package com.optifinecapes;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptifineCapesClient implements ClientModInitializer {
    public static final String MOD_ID = "optifinecapes";
    public static final Logger LOGGER = LoggerFactory.getLogger("OptiFine Capes");

    @Override
    public void onInitializeClient() {
        LOGGER.info("OptiFine Capes initialized");
    }
}
