package winord.visibleores.config;

public record OreSettings(boolean enabled, DimensionFilter dimension, boolean heightLimit, int minY, int maxY,
                          TextureMode texture, String customPack, boolean blueDebris, ApplyMode apply) {
    public static final int MIN_Y = -64;
    public static final int MAX_Y = 320;

    public static OreSettings defaults(OreType type) {
        return new OreSettings(type.isOre(), type.defaultDimension(), false, type.defaultMinY(), type.defaultMaxY(),
                TextureMode.STANDARD, "", false, type.defaultApply());
    }

    public OreSettings normalized(OreType type) {
        return new OreSettings(
                enabled,
                dimension,
                heightLimit,
                clamp(Math.min(minY, maxY)),
                clamp(Math.max(minY, maxY)),
                type.textureModes().contains(texture) ? texture : TextureMode.STANDARD,
                CustomPacks.normalize(customPack.trim()),
                blueDebris && type == OreType.ANCIENT_DEBRIS,
                apply);
    }

    public OreSettings withEnabled(boolean value) {
        return new OreSettings(value, dimension, heightLimit, minY, maxY, texture, customPack, blueDebris, apply);
    }

    public OreSettings withDimension(DimensionFilter value) {
        return new OreSettings(enabled, value, heightLimit, minY, maxY, texture, customPack, blueDebris, apply);
    }

    public OreSettings withHeight(boolean limit, int min, int max) {
        return new OreSettings(enabled, dimension, limit, min, max, texture, customPack, blueDebris, apply);
    }

    public OreSettings withTexture(TextureMode value) {
        return new OreSettings(enabled, dimension, heightLimit, minY, maxY, value, customPack, blueDebris, apply);
    }

    public OreSettings withPack(String value) {
        return new OreSettings(enabled, dimension, heightLimit, minY, maxY, texture, value, blueDebris, apply);
    }

    public OreSettings withBlueDebris(boolean value) {
        return new OreSettings(enabled, dimension, heightLimit, minY, maxY, texture, customPack, value, apply);
    }

    public OreSettings withApply(ApplyMode value) {
        return new OreSettings(enabled, dimension, heightLimit, minY, maxY, texture, customPack, blueDebris, value);
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
