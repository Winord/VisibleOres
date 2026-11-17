/*!stonecutter-disabled
package winord.visibleores.neoforge;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import winord.visibleores.platform.Platform;

import java.nio.file.Path;

public final class NeoForgePlatform implements Platform {
    @Override
    public boolean isModLoaded(String id) {
        var mods = ModList.get();
        if (mods != null) return mods.isLoaded(id);
        return FMLLoader.getCurrent().getLoadingModList().getModFileById(id) != null;
    }

    @Override
    public Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
*/