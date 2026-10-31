package winord.deepdebris.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;
import winord.deepdebris.config.DeepDebrisConfig;
import winord.deepdebris.config.OreType;

public final class DeepRule {
    private DeepRule() { }

    @Nullable
    public static TextureVariant resolve(OreType ore, BlockAndTintGetter level, BlockPos pos) {
        if (level == BlockAndTintGetter.EMPTY) return null;

        var snapshot = DeepDebrisConfig.get();
        if (!snapshot.enabled()) return null;

        var settings = snapshot.ore(ore);
        if (!settings.enabled()) return null;
        if (settings.heightLimit() && !settings.containsY(pos.getY())) return null;

        var world = Minecraft.getInstance().level;
        if (world == null || !settings.dimension().matches(world.dimension())) return null;

        return TextureVariant.of(settings);
    }
}
