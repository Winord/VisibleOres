package winord.deepdebris.render;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;
import winord.deepdebris.DeepDebris;

import java.io.IOException;
import java.util.Properties;

public final class EmissiveSuffix {
    public static final String DEFAULT = "_e";

    private static final Identifier LOCATION = Identifier.withDefaultNamespace("optifine/emissive.properties");
    private static final String KEY = "suffix.emissive";

    private EmissiveSuffix() { }

    @Nullable
    public static String of(ResourceManager manager) {
        var resource = manager.getResource(LOCATION);
        if (resource.isEmpty()) return null;

        try (var in = resource.get().open()) {
            var properties = new Properties();
            properties.load(in);
            var suffix = properties.getProperty(KEY);
            return suffix == null || suffix.isBlank() ? null : suffix.trim();
        } catch (IOException e) {
            DeepDebris.LOGGER.warn("Could not read {} from {}", LOCATION, resource.get().sourcePackId(), e);
            return null;
        }
    }
}
