package winord.visibleores.fabric;

import net.fabricmc.loader.api.FabricLoader;
import winord.visibleores.platform.Platform;

import java.nio.file.Path;

public final class FabricPlatform implements Platform {
    @Override
    public boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }

    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
