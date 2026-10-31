package winord.deepdebris;

import winord.deepdebris.config.DeepDebrisConfig;
import winord.deepdebris.input.KeybindHandler;

public final class DeepDebrisClient {
    private DeepDebrisClient() { }

    public static void initClient() {
        DeepDebrisConfig.load();
        KeybindHandler.register();
    }
}
