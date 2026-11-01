package winord.deepdebris.config;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Requirement;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.EnumListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import me.shedaniel.clothconfig2.gui.entries.KeyCodeEntry;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import winord.deepdebris.input.KeybindHandler;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static winord.deepdebris.DeepDebris.MOD_ID;

public final class ClothConfigScreenBuilder {
    private ClothConfigScreenBuilder() { }

    public static Screen build(Screen parent) {
        var builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable(key("title")));

        var entries = builder.entryBuilder();
        var current = DeepDebrisConfig.get();

        var masterEntry = entries
                .startBooleanToggle(Component.translatable(key("enabled")), current.enabled())
                .setDefaultValue(DeepDebrisConfig.DEFAULT_ENABLED)
                .setTooltip(Component.translatable(key("enabled.tooltip")))
                .build();
        builder.getOrCreateCategory(Component.translatable(key("category.general"))).addEntry(masterEntry);

        var oreEntries = new EnumMap<OreType, OreEntries>(OreType.class);
        for (var ore : OreType.values()) oreEntries.put(ore, new OreEntries(entries, ore, current.ore(ore)));

        var nether = builder.getOrCreateCategory(Component.translatable(key("category.nether")));
        for (var ore : OreType.values()) {
            if (ore.group() == OreGroup.NETHER) nether.addEntry(oreEntries.get(ore).subCategory());
        }

        var overworld = builder.getOrCreateCategory(Component.translatable(key("category.overworld")));
        for (var group : List.of(OreGroup.STONE, OreGroup.DEEPSLATE)) {
            var children = new ArrayList<AbstractConfigListEntry>();
            for (var ore : OreType.values()) {
                if (ore.group() == group) children.add(oreEntries.get(ore).subCategory());
            }
            overworld.addEntry(new GuardedSubCategory(Component.translatable(key("group." + group.name().toLowerCase(Locale.ROOT))), children));
        }

        var keybinds = builder.getOrCreateCategory(Component.translatable(key("category.keybinds")));
        var keyEntries = new HashMap<String, KeyCodeEntry>();
        var masterKey = keyField(entries, Component.translatable(key("keybinds.master")), current.keybind(DeepDebrisConfig.MASTER_KEY),
                Component.translatable(key("keybinds.master.tooltip")));
        keyEntries.put(DeepDebrisConfig.MASTER_KEY, masterKey);
        keybinds.addEntry(masterKey);
        for (var group : OreGroup.values()) {
            var groupBuilder = entries.startSubCategory(Component.translatable(key("group." + group.name().toLowerCase(Locale.ROOT))));
            groupBuilder.setExpanded(false);
            for (var ore : OreType.values()) {
                if (ore.group() != group) continue;
                var field = keyField(entries, ore.displayName(), current.keybind(ore.key()), Component.translatable(key("keybinds.ore.tooltip")));
                keyEntries.put(ore.key(), field);
                groupBuilder.add(field);
            }
            keybinds.addEntry(groupBuilder.build());
        }

        builder.setSavingRunnable(() -> {
            var next = current.withEnabled(masterEntry.getValue());
            for (var ore : OreType.values()) next = next.withOre(ore, oreEntries.get(ore).read());
            var keys = new HashMap<String, String>();
            for (var entry : keyEntries.entrySet()) keys.put(entry.getKey(), KeybindHandler.format(entry.getValue().getValue().getKeyCode()));
            DeepDebrisConfig.apply(new DeepDebrisConfig.Snapshot(next.enabled(), next.ores(), keys));
        });
        return builder.build();
    }

    private static KeyCodeEntry keyField(ConfigEntryBuilder entries, Component name, String stored, Component tooltip) {
        return entries
                .startKeyCodeField(name, KeybindHandler.parse(stored))
                .setAllowMouse(false)
                .setDefaultValue(InputConstants.UNKNOWN)
                .setTooltip(tooltip)
                .build();
    }

    private static String key(String name) {
        return "config." + MOD_ID + "." + name;
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
                    .startBooleanToggle(Component.translatable(key("ore.enabled")), current.enabled())
                    .setDefaultValue(defaults.enabled())
                    .setTooltip(Component.translatable(key("ore.enabled.tooltip")))
                    .build();

            dimension = entries
                    .startEnumSelector(Component.translatable(key("dimension")), DimensionFilter.class, current.dimension())
                    .setDefaultValue(defaults.dimension())
                    .setEnumNameProvider(value -> Component.translatable(key("dimension." + value.name().toLowerCase(Locale.ROOT))))
                    .setTooltip(Component.translatable(key("dimension.tooltip")))
                    .build();

            heightLimit = entries
                    .startBooleanToggle(Component.translatable(key("height_limit")), current.heightLimit())
                    .setDefaultValue(defaults.heightLimit())
                    .setTooltip(Component.translatable(key("height_limit.tooltip")))
                    .build();

            minY = entries
                    .startIntField(Component.translatable(key("min_y")), current.minY())
                    .setMin(DeepDebrisConfig.MIN_Y)
                    .setMax(DeepDebrisConfig.MAX_Y)
                    .setDefaultValue(defaults.minY())
                    .setTooltip(Component.translatable(key("min_y.tooltip")))
                    .setDisplayRequirement(Requirement.isTrue(heightLimit))
                    .build();

            maxY = entries
                    .startIntField(Component.translatable(key("max_y")), current.maxY())
                    .setMin(DeepDebrisConfig.MIN_Y)
                    .setMax(DeepDebrisConfig.MAX_Y)
                    .setDefaultValue(defaults.maxY())
                    .setTooltip(Component.translatable(key("max_y.tooltip")))
                    .setDisplayRequirement(Requirement.isTrue(heightLimit))
                    .build();

            texture = entries
                    .startEnumSelector(Component.translatable(key("texture")), TextureMode.class, current.texture())
                    .setDefaultValue(defaults.texture())
                    .setEnumNameProvider(value -> Component.translatable(key("texture." + value.name().toLowerCase(Locale.ROOT))))
                    .setTooltip(Component.translatable(key("texture.tooltip")))
                    .build();

            pack = entries
                    .startStringDropdownMenu(Component.translatable(key("custom_pack")), CustomPacks.normalize(current.customPack()), CustomPacks::displayName)
                    .setSelections(CustomPacks.selections(current.customPack()))
                    .setSuggestionMode(false)
                    .setDefaultValue(defaults.customPack())
                    .setTooltip(Component.translatable(key("custom_pack.tooltip")))
                    .setDisplayRequirement(Requirement.isValue(texture, TextureMode.CUSTOM))
                    .build();

            blue = ore == OreType.ANCIENT_DEBRIS
                    ? entries
                            .startBooleanToggle(Component.translatable(key("blue_debris")), current.blueDebris())
                            .setDefaultValue(defaults.blueDebris())
                            .setTooltip(Component.translatable(key("blue_debris.tooltip")))
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
                    texture.getValue(), CustomPacks.normalize(pack.getValue()), blue != null && blue.getValue());
        }
    }
}
