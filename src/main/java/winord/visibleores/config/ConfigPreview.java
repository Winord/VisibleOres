package winord.visibleores.config;

import org.jspecify.annotations.Nullable;

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
