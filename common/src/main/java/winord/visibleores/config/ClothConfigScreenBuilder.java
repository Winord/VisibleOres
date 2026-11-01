package winord.visibleores.config;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Requirement;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import me.shedaniel.clothconfig2.gui.entries.EnumListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import me.shedaniel.clothconfig2.gui.entries.KeyCodeEntry;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import winord.visibleores.input.KeybindHandler;
import winord.visibleores.util.Lang;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClothConfigScreenBuilder {
    private ClothConfigScreenBuilder() { }

    public static Screen build(Screen parent) {
        var builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Lang.config("title"));

        var entries = builder.entryBuilder();
        var current = VisibleOresConfig.get();

        var masterEntry = entries
                .startBooleanToggle(Lang.config("enabled"), current.enabled())
                .setDefaultValue(ConfigSnapshot.DEFAULT_ENABLED)
                .setTooltip(Lang.config("enabled.tooltip"))
                .build();
        builder.getOrCreateCategory(Lang.config("category.general")).addEntry(masterEntry);

        var oreEntries = new EnumMap<OreType, OreEntries>(OreType.class);
        for (var ore : OreType.values()) oreEntries.put(ore, new OreEntries(entries, ore, current.ore(ore)));

        addNetherCategory(builder.getOrCreateCategory(Lang.config("category.nether")), oreEntries);
        addOverworldCategory(builder.getOrCreateCategory(Lang.config("category.overworld")), oreEntries);
        var keyEntries = addKeybindsCategory(builder.getOrCreateCategory(Lang.config("category.keybinds")), entries, current);

        builder.setSavingRunnable(() -> {
            var ores = new EnumMap<OreType, OreSettings>(OreType.class);
            for (var ore : OreType.values()) ores.put(ore, oreEntries.get(ore).read());
            var keys = new HashMap<String, String>();
            for (var entry : keyEntries.entrySet()) keys.put(entry.getKey(), KeybindHandler.format(entry.getValue().getValue().getKeyCode()));
            VisibleOresConfig.apply(new ConfigSnapshot(masterEntry.getValue(), ores, keys));
        });
        return builder.build();
    }

    private static void addNetherCategory(ConfigCategory category, Map<OreType, OreEntries> oreEntries) {
        for (var ore : OreType.inGroup(OreGroup.NETHER)) category.addEntry(oreEntries.get(ore).subCategory());
    }

    private static void addOverworldCategory(ConfigCategory category, Map<OreType, OreEntries> oreEntries) {
        for (var group : List.of(OreGroup.STONE, OreGroup.DEEPSLATE)) {
            var children = new ArrayList<AbstractConfigListEntry>();
            for (var ore : OreType.inGroup(group)) children.add(oreEntries.get(ore).subCategory());
            category.addEntry(new GuardedSubCategory(Lang.configEnum("group", group), children));
        }
    }

    private static Map<String, KeyCodeEntry> addKeybindsCategory(ConfigCategory category, ConfigEntryBuilder entries, ConfigSnapshot current) {
        var keyEntries = new HashMap<String, KeyCodeEntry>();

        var master = keyField(entries, Lang.config("keybinds.master"), current.keybind(ConfigSnapshot.MASTER_KEY), Lang.config("keybinds.master.tooltip"));
        keyEntries.put(ConfigSnapshot.MASTER_KEY, master);
        category.addEntry(master);

        for (var group : OreGroup.values()) {
            var groupBuilder = entries.startSubCategory(Lang.configEnum("group", group));
            groupBuilder.setExpanded(false);
            for (var ore : OreType.inGroup(group)) {
                var field = keyField(entries, ore.displayName(), current.keybind(ore.key()), Lang.config("keybinds.ore.tooltip"));
                keyEntries.put(ore.key(), field);
                groupBuilder.add(field);
            }
            category.addEntry(groupBuilder.build());
        }
        return keyEntries;
    }

    private static KeyCodeEntry keyField(ConfigEntryBuilder entries, Component name, String stored, Component tooltip) {
        return entries
                .startKeyCodeField(name, KeybindHandler.parse(stored))
                .setAllowMouse(false)
                .setDefaultValue(InputConstants.UNKNOWN)
                .setTooltip(tooltip)
                .build();
    }

    private static final class OreEntries {
        private final OreType ore;
        private final BooleanListEntry enabled;
        private final EnumListEntry<DimensionFilter> dimension;
        private final BooleanListEntry heightLimit;
        private final IntegerListEntry minY;
        private final IntegerListEntry maxY;
        private final EnumListEntry<TextureMode> texture;
        private final DropdownBoxEntry<String> pack;
        private final BooleanListEntry blue;

        OreEntries(ConfigEntryBuilder entries, OreType ore, OreSettings current) {
            this.ore = ore;
            var defaults = OreSettings.defaults(ore);

            enabled = entries
                    .startBooleanToggle(Lang.config("ore.enabled"), current.enabled())
                    .setDefaultValue(defaults.enabled())
                    .setTooltip(Lang.config("ore.enabled.tooltip"))
                    .build();

            dimension = entries
                    .startEnumSelector(Lang.config("dimension"), DimensionFilter.class, current.dimension())
                    .setDefaultValue(defaults.dimension())
                    .setEnumNameProvider(value -> Lang.configEnum("dimension", value))
                    .setTooltip(Lang.config("dimension.tooltip"))
                    .build();

            heightLimit = entries
                    .startBooleanToggle(Lang.config("height_limit"), current.heightLimit())
                    .setDefaultValue(defaults.heightLimit())
                    .setTooltip(Lang.config("height_limit.tooltip"))
                    .build();

            minY = entries
                    .startIntField(Lang.config("min_y"), current.minY())
                    .setMin(OreSettings.MIN_Y)
                    .setMax(OreSettings.MAX_Y)
                    .setDefaultValue(defaults.minY())
                    .setTooltip(Lang.config("min_y.tooltip"))
                    .setDisplayRequirement(Requirement.isTrue(heightLimit))
                    .build();

            maxY = entries
                    .startIntField(Lang.config("max_y"), current.maxY())
                    .setMin(OreSettings.MIN_Y)
                    .setMax(OreSettings.MAX_Y)
                    .setDefaultValue(defaults.maxY())
                    .setTooltip(Lang.config("max_y.tooltip"))
                    .setDisplayRequirement(Requirement.isTrue(heightLimit))
                    .build();

            texture = entries
                    .startEnumSelector(Lang.config("texture"), TextureMode.class, current.texture())
                    .setDefaultValue(defaults.texture())
                    .setEnumNameProvider(value -> Lang.configEnum("texture", value))
                    .setTooltip(Lang.config("texture.tooltip"))
                    .build();

            pack = entries
                    .startStringDropdownMenu(Lang.config("custom_pack"), current.customPack(), CustomPacks::displayName)
                    .setSelections(CustomPacks.selections(current.customPack()))
                    .setSuggestionMode(false)
                    .setDefaultValue(defaults.customPack())
                    .setTooltip(Lang.config("custom_pack.tooltip"))
                    .setDisplayRequirement(Requirement.isValue(texture, TextureMode.CUSTOM))
                    .build();

            blue = ore == OreType.ANCIENT_DEBRIS
                    ? entries
                            .startBooleanToggle(Lang.config("blue_debris"), current.blueDebris())
                            .setDefaultValue(defaults.blueDebris())
                            .setTooltip(Lang.config("blue_debris.tooltip"))
                            .setDisplayRequirement(Requirement.not(Requirement.isValue(texture, TextureMode.CUSTOM)))
                            .build()
                    : null;
        }

        SubCategoryListEntry subCategory() {
            var children = new ArrayList<AbstractConfigListEntry>();
            children.add(enabled);
            children.add(dimension);
            children.add(heightLimit);
            children.add(minY);
            children.add(maxY);
            children.add(texture);
            children.add(pack);
            if (blue != null) children.add(blue);
            return new GuardedSubCategory(ore.displayName(), children);
        }

        OreSettings read() {
            return new OreSettings(enabled.getValue(), dimension.getValue(), heightLimit.getValue(), minY.getValue(), maxY.getValue(),
                    texture.getValue(), pack.getValue(), blue != null && blue.getValue());
        }
    }
}
