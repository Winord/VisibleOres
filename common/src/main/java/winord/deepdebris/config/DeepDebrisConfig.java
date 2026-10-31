package winord.deepdebris.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import winord.deepdebris.DeepDebris;
import winord.deepdebris.render.CustomPackTextureSource;
import winord.deepdebris.util.JsonFiles;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DeepDebrisConfig {
    public static final int MIN_Y = -64;
    public static final int MAX_Y = 320;
    public static final boolean DEFAULT_ENABLED = true;
    public static final String MASTER_KEY = "master";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = DeepDebris.CONFIG_DIR.resolve("settings.json");

    private static volatile Snapshot snapshot = Snapshot.defaults();

    private DeepDebrisConfig() { }

    public record Snapshot(boolean enabled, Map<OreType, OreSettings> ores, Map<String, String> keybinds) {
        public Snapshot {
            ores = Map.copyOf(ores);
            keybinds = Map.copyOf(keybinds);
        }

        static Snapshot defaults() {
            var ores = new EnumMap<OreType, OreSettings>(OreType.class);
            for (var type : OreType.values()) ores.put(type, OreSettings.defaults(type));
            return new Snapshot(DEFAULT_ENABLED, ores, Map.of());
        }

        public OreSettings ore(OreType type) {
            return ores.get(type);
        }

        public String keybind(String id) {
            return keybinds.getOrDefault(id, "");
        }

        public Snapshot withEnabled(boolean value) {
            return new Snapshot(value, ores, keybinds);
        }

        public Snapshot withOre(OreType type, OreSettings settings) {
            var next = new EnumMap<OreType, OreSettings>(OreType.class);
            next.putAll(ores);
            next.put(type, settings);
            return new Snapshot(enabled, next, keybinds);
        }

        Snapshot normalized() {
            var normalizedOres = new EnumMap<OreType, OreSettings>(OreType.class);
            for (var type : OreType.values()) {
                var settings = ores.get(type);
                normalizedOres.put(type, settings != null ? settings.normalized(type) : OreSettings.defaults(type));
            }
            var normalizedKeys = new HashMap<String, String>();
            for (var entry : keybinds.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isBlank()) normalizedKeys.put(entry.getKey(), entry.getValue().trim());
            }
            return new Snapshot(enabled, normalizedOres, normalizedKeys);
        }

        boolean sameRendering(Snapshot other) {
            return enabled == other.enabled && ores.equals(other.ores);
        }
    }

    private static final class Data {
        Boolean enabled;
        Map<String, OreData> ores;
        Map<String, String> keybinds;
    }

    private static final class OreData {
        Boolean enabled;
        DimensionFilter dimension;
        Boolean heightLimit;
        Integer minY;
        Integer maxY;
        TextureMode texture;
        String customPack;
        Boolean blueDebris;
    }

    public static Snapshot get() {
        return snapshot;
    }

    public static int clamp(int y) {
        return Math.max(MIN_Y, Math.min(MAX_Y, y));
    }

    public static void load() {
        if (!Files.exists(FILE)) {
            snapshot = Snapshot.defaults();
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            var data = GSON.fromJson(reader, Data.class);
            snapshot = fromData(data != null ? data : new Data());
            save();
        } catch (IOException | JsonSyntaxException | IllegalStateException e) {
            DeepDebris.LOGGER.warn("Could not read {}, using defaults", FILE, e);
            snapshot = Snapshot.defaults();
        }
    }

    public static void save() {
        try {
            JsonFiles.writeAtomically(GSON, FILE, toData(snapshot));
        } catch (IOException e) {
            DeepDebris.LOGGER.warn("Could not write {}", FILE, e);
        }
    }

    public static void apply(Snapshot requested) {
        var next = requested.normalized();
        var previous = snapshot;
        if (next.equals(previous)) return;

        snapshot = next;
        save();
        if (previous.sameRendering(next)) return;

        var minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (needsAtlasReload(next)) {
                minecraft.reloadResourcePacks();
            } else if (minecraft.level != null) {
                minecraft.levelRenderer.allChanged();
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

    private static boolean needsAtlasReload(Snapshot next) {
        for (var type : OreType.values()) {
            var settings = next.ore(type);
            if (settings.texture() == TextureMode.CUSTOM
                    && !settings.customPack().isEmpty()
                    && !settings.customPack().equals(CustomPackTextureSource.loadedPack(type))) {
                return true;
            }
        }
        return false;
    }

    private static Snapshot fromData(Data data) {
        var ores = new EnumMap<OreType, OreSettings>(OreType.class);
        for (var type : OreType.values()) {
            var defaults = OreSettings.defaults(type);
            var stored = data.ores != null ? data.ores.get(type.key()) : null;
            if (stored == null) {
                ores.put(type, defaults);
                continue;
            }
            ores.put(type, new OreSettings(
                    stored.enabled != null ? stored.enabled : defaults.enabled(),
                    stored.dimension != null ? stored.dimension : defaults.dimension(),
                    stored.heightLimit != null ? stored.heightLimit : defaults.heightLimit(),
                    stored.minY != null ? stored.minY : defaults.minY(),
                    stored.maxY != null ? stored.maxY : defaults.maxY(),
                    stored.texture != null ? stored.texture : defaults.texture(),
                    stored.customPack != null ? stored.customPack : defaults.customPack(),
                    stored.blueDebris != null ? stored.blueDebris : defaults.blueDebris()));
        }
        var keybinds = data.keybinds != null ? data.keybinds : Map.<String, String>of();
        return new Snapshot(data.enabled != null ? data.enabled : DEFAULT_ENABLED, ores, keybinds).normalized();
    }

    private static Data toData(Snapshot source) {
        var data = new Data();
        data.enabled = source.enabled();
        data.ores = new LinkedHashMap<>();
        data.keybinds = new LinkedHashMap<>();
        data.keybinds.put(MASTER_KEY, source.keybind(MASTER_KEY));
        for (var type : OreType.values()) {
            var settings = source.ore(type);
            var stored = new OreData();
            stored.enabled = settings.enabled();
            stored.dimension = settings.dimension();
            stored.heightLimit = settings.heightLimit();
            stored.minY = settings.minY();
            stored.maxY = settings.maxY();
            stored.texture = settings.texture();
            stored.customPack = settings.customPack();
            stored.blueDebris = settings.blueDebris();
            data.ores.put(type.key(), stored);
            data.keybinds.put(type.key(), source.keybind(type.key()));
        }
        return data;
    }
}
