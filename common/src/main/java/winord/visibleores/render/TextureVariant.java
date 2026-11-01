package winord.visibleores.render;

import net.minecraft.resources.Identifier;
import winord.visibleores.VisibleOres;
import winord.visibleores.config.OreSettings;

public enum TextureVariant {
    STANDARD("standard", false),
    FANCY("fancy", true),
    BLUE_STANDARD("blue_standard", false),
    BLUE_FANCY("blue_fancy", true),
    CUSTOM("custom", false);

    private final String directory;
    private final boolean emissive;

    TextureVariant(String directory, boolean emissive) {
        this.directory = directory;
        this.emissive = emissive;
    }

    public Identifier spriteId(String path) {
        return Identifier.fromNamespaceAndPath(VisibleOres.MOD_ID, "block/" + directory + "/" + path);
    }

    public boolean hasEmissiveSprites() {
        return emissive;
    }

    public TextureVariant bundled() {
        return this == CUSTOM ? STANDARD : this;
    }

    public static TextureVariant of(OreSettings settings) {
        return switch (settings.texture()) {
            case STANDARD -> settings.blueDebris() ? BLUE_STANDARD : STANDARD;
            case FANCY -> settings.blueDebris() ? BLUE_FANCY : FANCY;
            case CUSTOM -> CUSTOM;
        };
    }
}
