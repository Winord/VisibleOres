package winord.deepdebris.config;

public record OreSettings(boolean enabled, DimensionFilter dimension, boolean heightLimit, int minY, int maxY,
                          TextureMode texture, String customPack, boolean blueDebris) {
    public static OreSettings defaults(OreType type) {
        return new OreSettings(true, type.defaultDimension(), false, type.defaultMinY(), type.defaultMaxY(),
                TextureMode.STANDARD, "", false);
    }

    public OreSettings normalized(OreType type) {
        var low = DeepDebrisConfig.clamp(Math.min(minY, maxY));
        var high = DeepDebrisConfig.clamp(Math.max(minY, maxY));
        return new OreSettings(
                enabled,
                dimension != null ? dimension : type.defaultDimension(),
                heightLimit,
                low,
                high,
                texture != null ? texture : TextureMode.STANDARD,
                customPack != null ? CustomPacks.normalize(customPack.trim()) : "",
                blueDebris && type == OreType.ANCIENT_DEBRIS);
    }

    public OreSettings withEnabled(boolean value) {
        return new OreSettings(value, dimension, heightLimit, minY, maxY, texture, customPack, blueDebris);
    }

    public boolean containsY(int y) {
        return y >= minY && y <= maxY;
    }
}
