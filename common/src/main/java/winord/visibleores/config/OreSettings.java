package winord.visibleores.config;

public record OreSettings(boolean enabled, DimensionFilter dimension, boolean heightLimit, int minY, int maxY,
                          TextureMode texture, String customPack, boolean blueDebris) {
    public static final int MIN_Y = -64;
    public static final int MAX_Y = 320;

    public static OreSettings defaults(OreType type) {
        return new OreSettings(true, type.defaultDimension(), false, type.defaultMinY(), type.defaultMaxY(),
                TextureMode.STANDARD, "", false);
    }

    public OreSettings normalized(OreType type) {
        return new OreSettings(
                enabled,
                dimension,
                heightLimit,
                clamp(Math.min(minY, maxY)),
                clamp(Math.max(minY, maxY)),
                texture,
                CustomPacks.normalize(customPack.trim()),
                blueDebris && type == OreType.ANCIENT_DEBRIS);
    }

    public OreSettings withEnabled(boolean value) {
        return new OreSettings(value, dimension, heightLimit, minY, maxY, texture, customPack, blueDebris);
    }

    public boolean containsY(int y) {
        return y >= minY && y <= maxY;
    }

    public String selectedPack() {
        return texture == TextureMode.CUSTOM ? customPack : "";
    }

    private static int clamp(int y) {
        return Math.max(MIN_Y, Math.min(MAX_Y, y));
    }
}
