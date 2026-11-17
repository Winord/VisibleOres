package winord.visibleores.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import winord.visibleores.VisibleOres;
import winord.visibleores.platform.Platform;
import winord.visibleores.util.JsonFiles;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

final class ConfigStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = Platform.INSTANCE.configDir().resolve(VisibleOres.MOD_ID).resolve("settings.json");

    private ConfigStorage() { }

    static ConfigSnapshot load() {
        if (!Files.exists(FILE)) {
            var defaults = ConfigSnapshot.defaults();
            save(defaults);
            return defaults;
        }

        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            var data = GSON.fromJson(reader, FileData.class);
            var loaded = fromFile(data != null ? data : new FileData());
            save(loaded);
            return loaded;
        } catch (IOException | JsonSyntaxException | IllegalStateException e) {
            VisibleOres.LOGGER.warn("Could not read {}, using defaults", FILE, e);
            return ConfigSnapshot.defaults();
        }
    }

    static void save(ConfigSnapshot snapshot) {
        try {
            JsonFiles.writeAtomically(GSON, FILE, toFile(snapshot));
        } catch (IOException e) {
            VisibleOres.LOGGER.warn("Could not write {}", FILE, e);
        }
    }

    private static ConfigSnapshot fromFile(FileData data) {
        var ores = new EnumMap<OreType, OreSettings>(OreType.class);
        for (var type : OreType.values()) {
            var defaults = OreSettings.defaults(type);
            var stored = data.ores != null ? data.ores.get(type.key()) : null;
            ores.put(type, stored != null ? stored.toSettings(defaults) : defaults);
        }
        var enabled = Objects.requireNonNullElse(data.enabled, ConfigSnapshot.DEFAULT_ENABLED);
        var keybinds = new HashMap<String, String>();
        if (data.keybinds != null) {
            data.keybinds.forEach((id, key) -> {
                if (key != null) keybinds.put(id, key);
            });
        }
        return new ConfigSnapshot(enabled, ores, keybinds).normalized();
    }

    private static FileData toFile(ConfigSnapshot snapshot) {
        var data = new FileData();
        data.enabled = snapshot.enabled();
        data.ores = new LinkedHashMap<>();
        data.keybinds = new LinkedHashMap<>();
        data.keybinds.put(ConfigSnapshot.MASTER_KEY, snapshot.keybind(ConfigSnapshot.MASTER_KEY));
        for (var type : OreType.values()) {
            data.ores.put(type.key(), OreFileData.of(snapshot.ore(type)));
            data.keybinds.put(type.key(), snapshot.keybind(type.key()));
        }
        return data;
    }

    private static final class FileData {
        Boolean enabled;
        Map<String, OreFileData> ores;
        Map<String, String> keybinds;
    }

    private static final class OreFileData {
        Boolean enabled;
        DimensionFilter dimension;
        Boolean heightLimit;
        Integer minY;
        Integer maxY;
        TextureMode texture;
        String customPack;
        Boolean blueDebris;
        ApplyMode apply;

        static OreFileData of(OreSettings settings) {
            var data = new OreFileData();
            data.enabled = settings.enabled();
            data.dimension = settings.dimension();
            data.heightLimit = settings.heightLimit();
            data.minY = settings.minY();
            data.maxY = settings.maxY();
            data.texture = settings.texture();
            data.customPack = settings.customPack();
            data.blueDebris = settings.blueDebris();
            data.apply = settings.apply();
            return data;
        }

        OreSettings toSettings(OreSettings defaults) {
            return new OreSettings(
                    Objects.requireNonNullElse(enabled, defaults.enabled()),
                    Objects.requireNonNullElse(dimension, defaults.dimension()),
                    Objects.requireNonNullElse(heightLimit, defaults.heightLimit()),
                    Objects.requireNonNullElse(minY, defaults.minY()),
                    Objects.requireNonNullElse(maxY, defaults.maxY()),
                    Objects.requireNonNullElse(texture, defaults.texture()),
                    Objects.requireNonNullElse(customPack, defaults.customPack()),
                    Objects.requireNonNullElse(blueDebris, defaults.blueDebris()),
                    Objects.requireNonNullElse(apply, defaults.apply()));
        }
    }
}
