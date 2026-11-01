package winord.visibleores.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum OreType {
    ANCIENT_DEBRIS("ancient_debris", OreGroup.NETHER, DimensionFilter.NETHER, 8, 119, "ancient_debris_side", "ancient_debris_top"),
    NETHER_QUARTZ_ORE("nether_quartz_ore", OreGroup.NETHER, DimensionFilter.NETHER, 10, 117),
    NETHER_GOLD_ORE("nether_gold_ore", OreGroup.NETHER, DimensionFilter.NETHER, 10, 117),
    GILDED_BLACKSTONE("gilded_blackstone", OreGroup.NETHER, DimensionFilter.NETHER, 20, 100),

    REDSTONE_ORE("redstone_ore", OreGroup.STONE, DimensionFilter.OVERWORLD, 0, 15),
    LAPIS_ORE("lapis_ore", OreGroup.STONE, DimensionFilter.OVERWORLD, 0, 64),
    IRON_ORE("iron_ore", OreGroup.STONE, DimensionFilter.OVERWORLD, 0, 319),
    GOLD_ORE("gold_ore", OreGroup.STONE, DimensionFilter.OVERWORLD, 0, 256),
    EMERALD_ORE("emerald_ore", OreGroup.STONE, DimensionFilter.OVERWORLD, 0, 319),
    DIAMOND_ORE("diamond_ore", OreGroup.STONE, DimensionFilter.OVERWORLD, 0, 16),
    COPPER_ORE("copper_ore", OreGroup.STONE, DimensionFilter.OVERWORLD, 0, 112),
    COAL_ORE("coal_ore", OreGroup.STONE, DimensionFilter.OVERWORLD, 0, 319),

    DEEPSLATE_REDSTONE_ORE("deepslate_redstone_ore", OreGroup.DEEPSLATE, DimensionFilter.OVERWORLD, -64, 7),
    DEEPSLATE_LAPIS_ORE("deepslate_lapis_ore", OreGroup.DEEPSLATE, DimensionFilter.OVERWORLD, -64, 7),
    DEEPSLATE_IRON_ORE("deepslate_iron_ore", OreGroup.DEEPSLATE, DimensionFilter.OVERWORLD, -64, 7),
    DEEPSLATE_GOLD_ORE("deepslate_gold_ore", OreGroup.DEEPSLATE, DimensionFilter.OVERWORLD, -64, 7),
    DEEPSLATE_EMERALD_ORE("deepslate_emerald_ore", OreGroup.DEEPSLATE, DimensionFilter.OVERWORLD, -16, 7),
    DEEPSLATE_DIAMOND_ORE("deepslate_diamond_ore", OreGroup.DEEPSLATE, DimensionFilter.OVERWORLD, -64, 7),
    DEEPSLATE_COPPER_ORE("deepslate_copper_ore", OreGroup.DEEPSLATE, DimensionFilter.OVERWORLD, -16, 7),
    DEEPSLATE_COAL_ORE("deepslate_coal_ore", OreGroup.DEEPSLATE, DimensionFilter.OVERWORLD, 0, 7);

    private static final Map<String, OreType> BY_KEY = new HashMap<>();

    static {
        for (var type : values()) BY_KEY.put(type.key, type);
    }

    private final String key;
    private final OreGroup group;
    private final DimensionFilter defaultDimension;
    private final int defaultMinY;
    private final int defaultMaxY;
    private final List<String> slots;

    OreType(String key, OreGroup group, DimensionFilter defaultDimension, int defaultMinY, int defaultMaxY, String... slots) {
        this.key = key;
        this.group = group;
        this.defaultDimension = defaultDimension;
        this.defaultMinY = defaultMinY;
        this.defaultMaxY = defaultMaxY;
        this.slots = slots.length == 0 ? List.of(key) : List.of(slots);
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

    public List<String> slots() {
        return slots;
    }

    public Component displayName() {
        return BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(key)).getName();
    }

    public static List<OreType> inGroup(OreGroup group) {
        return Arrays.stream(values()).filter(type -> type.group == group).toList();
    }

    @Nullable
    public static OreType of(BlockState state) {
        var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (!Identifier.DEFAULT_NAMESPACE.equals(id.getNamespace())) return null;
        return BY_KEY.get(id.getPath());
    }
}
