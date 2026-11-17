package winord.visibleores.config;

import org.jspecify.annotations.Nullable;

/**
 * Settings the item renderer should use right now. The config screens draw items with their unsaved draft,
 * so they swap it in for the duration of the draw call; everything else sees the saved config.
 */
public final class ConfigPreview {
    @Nullable
    private static ConfigSnapshot override;

    private ConfigPreview() { }

    public static ConfigSnapshot current() {
        var preview = override;
        return preview != null ? preview : VisibleOresConfig.get();
    }

    public static void with(ConfigSnapshot snapshot, Runnable draw) {
        var previous = override;
        override = snapshot;
        try {
            draw.run();
        } finally {
            override = previous;
        }
    }
}
