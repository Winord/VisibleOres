package winord.visibleores.config;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public enum DimensionFilter {
    NETHER,
    OVERWORLD,
    ANY;

    public boolean matches(ResourceKey<Level> dimension) {
        return switch (this) {
            case NETHER -> dimension == Level.NETHER;
            case OVERWORLD -> dimension == Level.OVERWORLD;
            case ANY -> dimension == Level.NETHER || dimension == Level.OVERWORLD;
        };
    }
}
