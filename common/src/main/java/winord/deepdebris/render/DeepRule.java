package winord.deepdebris.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import winord.deepdebris.config.DeepDebrisConfig;

public final class DeepRule {
    private DeepRule() { }

    public static boolean applies(BlockAndTintGetter level, BlockPos pos) {
        if (level == BlockAndTintGetter.EMPTY) return false;

        var snapshot = DeepDebrisConfig.get();
        if (!snapshot.enabled() || pos.getY() >= snapshot.threshold()) return false;

        var world = Minecraft.getInstance().level;
        return world != null && world.dimension() == Level.NETHER;
    }
}
