package winord.deepdebris;

import winord.deepdebris.config.DeepDebrisConfig;

public final class DeepDebrisClient {
    private DeepDebrisClient() { }

    public static void initClient() {
        DeepDebrisConfig.load();
    }
}
