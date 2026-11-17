package winord.visibleores.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import winord.visibleores.util.Lang;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public enum OreType {
    ANCIENT_DEBRIS(ore("ancient_debris", OreGroup.NETHER, DimensionFilter.NETHER, 8, 119)
            .textures("ancient_debris_side", "ancient_debris_top")),
    NETHER_QUARTZ_ORE(ore("nether_quartz_ore", OreGroup.NETHER, DimensionFilter.NETHER, 10, 117)),
    NETHER_GOLD_ORE(ore("nether_gold_ore", OreGroup.NETHER, DimensionFilter.NETHER, 10, 117)),
    GILDED_BLACKSTONE(ore("gilded_blackstone", OreGroup.NETHER, DimensionFilter.NETHER, 20, 100)),

    REDSTONE_ORE(ore("redstone_ore", OreGroup.STONE, DimensionFilter.OVERWORLD, 0, 15)),
    LAPIS_ORE(ore("lapis_ore", OreGroup.STONE, DimensionFilter.OVERWORLD, 0, 64)),
    IRON_ORE(ore("iron_ore", OreGroup.STONE, DimensionFilter.OVERWORLD, 0, 319)),
    GOLD_ORE(ore("gold_ore", OreGroup.STONE, DimensionFilter.OVERWORLD, 0, 256)),
    EMERALD_ORE(ore("emerald_ore", OreGroup.STONE, DimensionFilter.OVERWORLD, 0, 319)),
    DIAMOND_ORE(ore("diamond_ore", OreGroup.STONE, DimensionFilter.OVERWORLD, 0, 16)),
    COPPER_ORE(ore("copper_ore", OreGroup.STONE, DimensionFilter.OVERWORLD, 0, 112)),
    COAL_ORE(ore("coal_ore", OreGroup.STONE, DimensionFilter.OVERWORLD, 0, 319)),

    DEEPSLATE_REDSTONE_ORE(ore("deepslate_redstone_ore", OreGroup.DEEPSLATE, DimensionFilter.OVERWORLD, -64, 7)),
    DEEPSLATE_LAPIS_ORE(ore("deepslate_lapis_ore", OreGroup.DEEPSLATE, DimensionFilter.OVERWORLD, -64, 7)),
    DEEPSLATE_IRON_ORE(ore("deepslate_iron_ore", OreGroup.DEEPSLATE, DimensionFilter.OVERWORLD, -64, 7)),
    DEEPSLATE_GOLD_ORE(ore("deepslate_gold_ore", OreGroup.DEEPSLATE, DimensionFilter.OVERWORLD, -64, 7)),
    DEEPSLATE_EMERALD_ORE(ore("deepslate_emerald_ore", OreGroup.DEEPSLATE, DimensionFilter.OVERWORLD, -16, 7)),
    DEEPSLATE_DIAMOND_ORE(ore("deepslate_diamond_ore", OreGroup.DEEPSLATE, DimensionFilter.OVERWORLD, -64, 7)),
    DEEPSLATE_COPPER_ORE(ore("deepslate_copper_ore", OreGroup.DEEPSLATE, DimensionFilter.OVERWORLD, -16, 7)),
    DEEPSLATE_COAL_ORE(ore("deepslate_coal_ore", OreGroup.DEEPSLATE, DimensionFilter.OVERWORLD, 0, 7)),

    SUSPICIOUS_SAND(other("suspicious_sand", DimensionFilter.OVERWORLD)
            .textures("suspicious_sand_0", "suspicious_sand_1", "suspicious_sand_2", "suspicious_sand_3")),
    SUSPICIOUS_GRAVEL(other("suspicious_gravel", DimensionFilter.OVERWORLD)
            .textures("suspicious_gravel_0", "suspicious_gravel_1", "suspicious_gravel_2", "suspicious_gravel_3")),
    BUDDING_AMETHYST(other("budding_amethyst", DimensionFilter.OVERWORLD)),
    INFESTED_BLOCKS(other("infested_blocks", DimensionFilter.OVERWORLD)
            .family(path -> path.startsWith("infested_"), "infested_stone")
            .replace("stone", "infested_stone")
            .replace("cobblestone", "infested_cobblestone")
            .replace("stone_bricks", "infested_stone_bricks")
            .replace("mossy_stone_bricks", "infested_mossy_stone_bricks")
            .replace("cracked_stone_bricks", "infested_cracked_stone_bricks")
            .replace("chiseled_stone_bricks", "infested_chiseled_stone_bricks")
            .replace("deepslate", "infested_deepslate")
            .replace("deepslate_top", "infested_deepslate_top")),
    WAXED_COPPER(waxedCopper(other("waxed_copper", DimensionFilter.ANY)
            .family(path -> path.startsWith("waxed_"), "waxed_copper_block")
            .apply(ApplyMode.ITEM)));

    private static final Map<String, OreType> BY_KEY = new LinkedHashMap<>();

    static {
        for (var type : values()) {
            if (type.predicate == null) BY_KEY.put(type.key, type);
        }
    }

    private final String key;
    private final OreGroup group;
    private final DimensionFilter defaultDimension;
    private final int defaultMinY;
    private final int defaultMaxY;
    private final boolean ore;
    private final ApplyMode defaultApply;
    private final Map<String, String> textures;
    @Nullable
    private final Predicate<String> predicate;
    private final String iconBlock;

    OreType(Builder builder) {
        this.key = builder.key;
        this.group = builder.group;
        this.defaultDimension = builder.dimension;
        this.defaultMinY = builder.minY;
        this.defaultMaxY = builder.maxY;
        this.ore = builder.ore;
        this.defaultApply = builder.apply;
        this.textures = builder.textures.isEmpty() ? Map.of(builder.key, builder.key) : Map.copyOf(builder.textures);
        this.predicate = builder.predicate;
        this.iconBlock = builder.iconBlock;
    }

    public String key() {
        return key;
    }

    public OreGroup group() {
        return group;
    }

    public DimensionFilter defaultDimension() {
        return defaultDimension;
    }

    public int defaultMinY() {
        return defaultMinY;
    }

    public int defaultMaxY() {
        return defaultMaxY;
    }

    public boolean isOre() {
        return ore;
    }

    public ApplyMode defaultApply() {
        return defaultApply;
    }

    public Map<String, String> textures() {
        return textures;
    }

    public List<TextureMode> textureModes() {
        return ore ? List.of(TextureMode.values()) : List.of(TextureMode.STANDARD, TextureMode.CUSTOM);
    }

    public Block iconBlock() {
        return BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(iconBlock));
    }

    public List<Block> blocks() {
        if (predicate == null) return List.of(iconBlock());
        return BuiltInRegistries.BLOCK.stream().filter(block -> covers(BuiltInRegistries.BLOCK.getKey(block))).toList();
    }

    public Component displayName() {
        return predicate == null ? iconBlock().getName() : Lang.config("block." + key);
    }

    public static List<OreType> inGroup(OreGroup group) {
        return Arrays.stream(values()).filter(type -> type.group == group).toList();
    }

    @Nullable
    public static OreType of(BlockState state) {
        return of(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    @Nullable
    public static OreType of(Identifier id) {
        if (!Identifier.DEFAULT_NAMESPACE.equals(id.getNamespace())) return null;
        var exact = BY_KEY.get(id.getPath());
        if (exact != null) return exact;
        for (var type : values()) {
            if (type.predicate != null && type.predicate.test(id.getPath())) return type;
        }
        return null;
    }

    private boolean covers(Identifier id) {
        return Identifier.DEFAULT_NAMESPACE.equals(id.getNamespace()) && predicate != null && predicate.test(id.getPath());
    }

    private static Builder ore(String key, OreGroup group, DimensionFilter dimension, int minY, int maxY) {
        var builder = new Builder(key, group, dimension);
        builder.minY = minY;
        builder.maxY = maxY;
        builder.ore = true;
        return builder;
    }

    private static Builder other(String key, DimensionFilter dimension) {
        return new Builder(key, OreGroup.OTHER, dimension);
    }

    private static Builder waxedCopper(Builder builder) {
        for (var oxidation : List.of("", "exposed_", "weathered_", "oxidized_")) {
            builder.replace(oxidation.isEmpty() ? "copper_block" : oxidation + "copper", "waxed_" + (oxidation.isEmpty() ? "copper_block" : oxidation + "copper"));
            for (var shape : List.of("cut_copper", "chiseled_copper", "copper_grate", "copper_trapdoor",
                    "copper_bulb", "copper_bulb_lit", "copper_bulb_powered", "copper_bulb_lit_powered",
                    "copper_bars", "copper_chain", "copper_door_top", "copper_door_bottom", "copper_lantern", "lightning_rod")) {
                builder.replace(oxidation + shape, "waxed_" + oxidation + shape);
            }
        }
        return builder.replace("lightning_rod_on", "waxed_lightning_rod_on");
    }

    private static final class Builder {
        private final String key;
        private final OreGroup group;
        private final DimensionFilter dimension;
        private int minY = OreSettings.MIN_Y;
        private int maxY = OreSettings.MAX_Y;
        private boolean ore;
        private ApplyMode apply = ApplyMode.BLOCK;
        private final Map<String, String> textures = new LinkedHashMap<>();
        @Nullable
        private Predicate<String> predicate;
        private String iconBlock;

        Builder(String key, OreGroup group, DimensionFilter dimension) {
            this.key = key;
            this.group = group;
            this.dimension = dimension;
            this.iconBlock = key;
        }

        Builder textures(String... names) {
            for (var name : names) textures.put(name, name);
            return this;
        }

        Builder replace(String vanilla, String replacement) {
            textures.put(vanilla, replacement);
            return this;
        }

        Builder family(Predicate<String> blocks, String icon) {
            this.predicate = blocks;
            this.iconBlock = icon;
            return this;
        }

        Builder apply(ApplyMode mode) {
            this.apply = mode;
            return this;
        }
    }
}
