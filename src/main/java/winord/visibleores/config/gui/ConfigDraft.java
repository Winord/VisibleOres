package winord.visibleores.config.gui;

import winord.visibleores.config.ConfigSnapshot;
import winord.visibleores.config.OreSettings;
import winord.visibleores.config.OreType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

final class ConfigDraft {
    boolean enabled;
    private final Map<OreType, OreSettings> ores = new EnumMap<>(OreType.class);
    private final Map<String, String> keybinds = new HashMap<>();

    ConfigDraft(ConfigSnapshot snapshot) {
        enabled = snapshot.enabled();
        ores.putAll(snapshot.ores());
        keybinds.putAll(snapshot.keybinds());
    }

    OreSettings ore(OreType type) {
        return ores.get(type);
    }

    void setOre(OreType type, OreSettings settings) {
        ores.put(type, settings);
    }

    String keybind(String id) {
        return keybinds.getOrDefault(id, "");
    }

    void setKeybind(String id, String key) {
        if (key.isEmpty()) keybinds.remove(id);
        else keybinds.put(id, key);
    }

    int keyUsages(String key) {
        if (key.isEmpty()) return 0;
        var count = 0;
        for (var value : keybinds.values()) if (value.equals(key)) count++;
        return count;
    }

    void clearKeybinds() {
        keybinds.clear();
    }

    void resetOres() {
        enabled = ConfigSnapshot.DEFAULT_ENABLED;
        for (var type : OreType.values()) ores.put(type, OreSettings.defaults(type));
    }

    ConfigSnapshot toSnapshot() {
        return new ConfigSnapshot(enabled, ores, keybinds);
    }
}
