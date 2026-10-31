package winord.deepdebris.render;

import net.minecraft.resources.Identifier;
import winord.deepdebris.DeepDebris;
import winord.deepdebris.config.OreSettings;
import winord.deepdebris.config.TextureMode;

public enum TextureVariant {
    STANDARD("standard"),
    FANCY("fancy"),
    BLUE_STANDARD("blue_standard"),
    BLUE_FANCY("blue_fancy"),
    CUSTOM("custom");

    private final String directory;

    TextureVariant(String directory) {
        this.directory = directory;
    }

    public Identifier spriteId(String slot) {
        return Identifier.fromNamespaceAndPath(DeepDebris.MOD_ID, "block/" + directory + "/" + slot);
    }

    public static TextureVariant of(OreSettings settings) {
        return switch (settings.texture()) {
            case STANDARD -> settings.blueDebris() ? BLUE_STANDARD : STANDARD;
            case FANCY -> settings.blueDebris() ? BLUE_FANCY : FANCY;
            case CUSTOM -> CUSTOM;
        };
    }
}
