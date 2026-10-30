package winord.deepdebris.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import winord.deepdebris.config.DeepDebrisConfig;

public final class DeepRule {
    private DeepRule() { }

    @Nullable
    public static RemapKind kind(BlockAndTintGetter level, BlockPos pos) {
        var snapshot = DeepDebrisConfig.get();
        if (!snapshot.enabled()) return null;

        if (!inZone(level, pos, snapshot.threshold())) return null;
        return switch (snapshot.source()) {
            case DEFAULT -> RemapKind.DEEP;
            case CUSTOM -> RemapKind.CUSTOM;
        };
    }

    private static boolean inZone(BlockAndTintGetter level, BlockPos pos, int threshold) {
        if (level == BlockAndTintGetter.EMPTY || pos.getY() >= threshold) return false;

        var world = Minecraft.getInstance().level;
        return world != null && world.dimension() == Level.NETHER;
    }
}
