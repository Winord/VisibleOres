package winord.deepdebris.fabric;

import net.fabricmc.api.ModInitializer;
import winord.deepdebris.DeepDebris;

public final class DeepDebrisFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DeepDebris.init();
    }
}
