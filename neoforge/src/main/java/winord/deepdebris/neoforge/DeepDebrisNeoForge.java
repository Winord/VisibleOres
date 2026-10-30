package winord.deepdebris.neoforge;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import winord.deepdebris.DeepDebris;

@Mod(DeepDebris.MOD_ID)
public final class DeepDebrisNeoForge {
    public DeepDebrisNeoForge(ModContainer container) {
        DeepDebris.init();
        if (Platform.getEnvironment() == Env.CLIENT) DeepDebrisNeoForgeClient.init(container);
    }
}
