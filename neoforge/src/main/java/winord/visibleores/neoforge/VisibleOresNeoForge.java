package winord.visibleores.neoforge;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import winord.visibleores.VisibleOres;

@Mod(VisibleOres.MOD_ID)
public final class VisibleOresNeoForge {
    public VisibleOresNeoForge(ModContainer container) {
        if (Platform.getEnvironment() == Env.CLIENT) VisibleOresNeoForgeClient.init(container);
    }
}
