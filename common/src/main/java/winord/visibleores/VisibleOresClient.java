package winord.visibleores;

import winord.visibleores.config.VisibleOresConfig;
import winord.visibleores.input.KeybindHandler;

public final class VisibleOresClient {
    private VisibleOresClient() { }

    public static void initClient() {
        VisibleOresConfig.load();
        KeybindHandler.register();
        VisibleOres.LOGGER.info("Initialized Visible Ores");
    }
}
