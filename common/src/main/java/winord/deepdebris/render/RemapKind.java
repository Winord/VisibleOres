package winord.deepdebris.render;

import net.minecraft.resources.Identifier;
import winord.deepdebris.DeepDebris;

import java.util.Map;

public enum RemapKind {
    DEEP("ancient_debris_deep_side", "ancient_debris_deep_top"),
    CUSTOM("ancient_debris_custom_side", "ancient_debris_custom_top");

    private final Map<Identifier, Identifier> targets;

    RemapKind(String side, String top) {
        this.targets = Map.of(
                Identifier.withDefaultNamespace("block/ancient_debris_side"),
                Identifier.fromNamespaceAndPath(DeepDebris.MOD_ID, "block/" + side),
                Identifier.withDefaultNamespace("block/ancient_debris_top"),
                Identifier.fromNamespaceAndPath(DeepDebris.MOD_ID, "block/" + top));
    }

    public Map<Identifier, Identifier> targets() {
        return targets;
    }
}
