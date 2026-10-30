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

public final class DeepDebrisConfig {
    public static final int MIN_Y = -64;
    public static final int MAX_Y = 320;
    public static final int DEFAULT_THRESHOLD = 16;
    public static final boolean DEFAULT_ENABLED = true;
    public static final TextureSource DEFAULT_SOURCE = TextureSource.DEFAULT;
    public static final String DEFAULT_CUSTOM_PACK = "";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = DeepDebris.CONFIG_DIR.resolve("settings.json");

    private static volatile Snapshot snapshot = new Snapshot(DEFAULT_ENABLED, DEFAULT_THRESHOLD, DEFAULT_SOURCE, DEFAULT_CUSTOM_PACK);

    private DeepDebrisConfig() { }

    public record Snapshot(boolean enabled, int threshold, TextureSource source, String customPack) { }

    private static final class Data {
        Boolean enabled;
        Integer depthThreshold;
        TextureSource textureSource;
        String customPack;
    }

    public static Snapshot get() {
        return snapshot;
    }

    public static int clamp(int threshold) {
        return Math.max(MIN_Y, Math.min(MAX_Y, threshold));
    }

    public static void load() {
        if (!Files.exists(FILE)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            var data = GSON.fromJson(reader, Data.class);
            if (data == null) data = new Data();
            snapshot = new Snapshot(
                    data.enabled != null ? data.enabled : DEFAULT_ENABLED,
                    data.depthThreshold != null ? clamp(data.depthThreshold) : DEFAULT_THRESHOLD,
                    data.textureSource != null ? data.textureSource : DEFAULT_SOURCE,
                    data.customPack != null ? data.customPack : DEFAULT_CUSTOM_PACK);
        } catch (IOException | JsonSyntaxException | IllegalStateException e) {
            DeepDebris.LOGGER.warn("Could not read {}, using defaults", FILE, e);
            snapshot = new Snapshot(DEFAULT_ENABLED, DEFAULT_THRESHOLD, DEFAULT_SOURCE, DEFAULT_CUSTOM_PACK);
        }
    }

    public static void save() {
        var current = snapshot;
        var data = new Data();
        data.enabled = current.enabled();
        data.depthThreshold = current.threshold();
        data.textureSource = current.source();
        data.customPack = current.customPack();
        try {
            JsonFiles.writeAtomically(GSON, FILE, data);
        } catch (IOException e) {
            DeepDebris.LOGGER.warn("Could not write {}", FILE, e);
        }
    }

    public static void apply(boolean enabled, int threshold, TextureSource source, String customPack) {
        var next = new Snapshot(enabled, clamp(threshold), source, customPack);
        if (next.equals(snapshot)) return;

        snapshot = next;
        save();

        var minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            var needsAtlasReload = next.source() == TextureSource.CUSTOM
                    && !next.customPack().isEmpty()
                    && !next.customPack().equals(CustomPackTextureSource.loadedPack());
            if (needsAtlasReload) {
                minecraft.reloadResourcePacks();
            } else if (minecraft.level != null) {
                minecraft.levelRenderer.allChanged();
            }
        });
    }
}
