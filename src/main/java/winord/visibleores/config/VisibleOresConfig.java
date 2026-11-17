package winord.visibleores.config;

import net.minecraft.client.Minecraft;
import winord.visibleores.render.OreSpriteSource;

public final class VisibleOresConfig {
    private static volatile ConfigSnapshot snapshot = ConfigSnapshot.defaults();

    private VisibleOresConfig() { }

    public static ConfigSnapshot get() {
        return snapshot;
    }

    public static void load() {
        snapshot = ConfigStorage.load();
    }

    public static void apply(ConfigSnapshot requested) {
        var next = requested.normalized();
        var previous = snapshot;
        if (next.equals(previous)) return;

        snapshot = next;
        ConfigStorage.save(next);
        if (previous.sameRendering(next)) return;

        var minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (OreSpriteSource.needsReload(next)) {
                minecraft.reloadResourcePacks();
            } else if (minecraft.level != null) {
                minecraft.levelExtractor.allChanged();
            }
        });
    }

    public static boolean toggleOre(OreType type) {
        var current = snapshot;
        var updated = current.ore(type).withEnabled(!current.ore(type).enabled());
        apply(current.withOre(type, updated));
        return updated.enabled();
    }

    public static boolean toggleMaster() {
        var current = snapshot;
        apply(current.withEnabled(!current.enabled()));
        return !current.enabled();
    }
}
