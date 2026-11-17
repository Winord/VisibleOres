package winord.visibleores.platform;

import java.nio.file.Path;

public interface Platform {
    Platform INSTANCE =
            /*? if fabric {*/new winord.visibleores.fabric.FabricPlatform();
            /*?} else *///new winord.visibleores.neoforge.NeoForgePlatform();

    boolean isModLoaded(String id);

    Path configDir();
}
