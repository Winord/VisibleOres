package winord.visibleores.config;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public record ConfigSnapshot(boolean enabled, Map<OreType, OreSettings> ores, Map<String, String> keybinds) {
    public static final boolean DEFAULT_ENABLED = true;
    public static final String MASTER_KEY = "master";

    public ConfigSnapshot {
        ores = Map.copyOf(ores);
        keybinds = Map.copyOf(keybinds);
    }

    static ConfigSnapshot defaults() {
        var ores = new EnumMap<OreType, OreSettings>(OreType.class);
        for (var type : OreType.values()) ores.put(type, OreSettings.defaults(type));
        return new ConfigSnapshot(DEFAULT_ENABLED, ores, Map.of());
    }

    public OreSettings ore(OreType type) {
        return ores.get(type);
    }

    public String keybind(String id) {
        return keybinds.getOrDefault(id, "");
    }

    public ConfigSnapshot withEnabled(boolean value) {
        return new ConfigSnapshot(value, ores, keybinds);
    }

    public ConfigSnapshot withOre(OreType type, OreSettings settings) {
        var next = new EnumMap<OreType, OreSettings>(OreType.class);
        next.putAll(ores);
        next.put(type, settings);
        return new ConfigSnapshot(enabled, next, keybinds);
    }

    ConfigSnapshot normalized() {
        var normalizedOres = new EnumMap<OreType, OreSettings>(OreType.class);
        for (var type : OreType.values()) {
            var settings = ores.get(type);
            normalizedOres.put(type, settings != null ? settings.normalized(type) : OreSettings.defaults(type));
        }
        var normalizedKeys = new HashMap<String, String>();
        for (var entry : keybinds.entrySet()) {
            if (!entry.getValue().isBlank()) normalizedKeys.put(entry.getKey(), entry.getValue().trim());
        }
        return new ConfigSnapshot(enabled, normalizedOres, normalizedKeys);
    }

    boolean sameRendering(ConfigSnapshot other) {
        return enabled == other.enabled && ores.equals(other.ores);
    }
}
