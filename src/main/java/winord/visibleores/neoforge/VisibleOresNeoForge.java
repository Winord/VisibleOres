/*!stonecutter-disabled
package winord.visibleores.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import winord.visibleores.VisibleOres;

@Mod(VisibleOres.MOD_ID)
public final class VisibleOresNeoForge {
    public VisibleOresNeoForge(ModContainer container, Dist dist) {
        if (dist.isClient()) VisibleOresNeoForgeClient.init(container);
    }
}
*/