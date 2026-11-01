package winord.visibleores.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;
import winord.visibleores.config.OreType;
import winord.visibleores.config.VisibleOresConfig;

public final class TextureRule {
    private TextureRule() { }

    @Nullable
    public static TextureVariant resolve(OreType ore, BlockAndTintGetter level, BlockPos pos) {
        if (level == BlockAndTintGetter.EMPTY) return null;

        var snapshot = VisibleOresConfig.get();
        if (!snapshot.enabled()) return null;

        var settings = snapshot.ore(ore);
        if (!settings.enabled()) return null;
        if (settings.heightLimit() && !settings.containsY(pos.getY())) return null;

        var world = Minecraft.getInstance().level;
        if (world == null || !settings.dimension().matches(world.dimension())) return null;

        return TextureVariant.of(settings);
    }
}
