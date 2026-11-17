package winord.visibleores;

import winord.visibleores.config.VisibleOresConfig;

public final class VisibleOresClient {
    private VisibleOresClient() { }

    public static void initClient() {
        VisibleOresConfig.load();
        VisibleOres.LOGGER.info("Initialized Visible Ores");
    }
}
