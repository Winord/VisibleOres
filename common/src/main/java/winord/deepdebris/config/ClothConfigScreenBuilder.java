package winord.deepdebris.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.Requirement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

import static winord.deepdebris.DeepDebris.MOD_ID;

public final class ClothConfigScreenBuilder {
    private ClothConfigScreenBuilder() { }

    public static Screen build(Screen parent) {
        var builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable(key("title")));

        var entryBuilder = builder.entryBuilder();
        var current = DeepDebrisConfig.get();

        var general = builder.getOrCreateCategory(Component.translatable(key("category.general")));

        var enabledEntry = entryBuilder
                .startBooleanToggle(Component.translatable(key("enabled")), current.enabled())
                .setDefaultValue(DeepDebrisConfig.DEFAULT_ENABLED)
                .setTooltip(Component.translatable(key("enabled.tooltip")))
                .build();
        var thresholdEntry = entryBuilder
                .startIntSlider(Component.translatable(key("depth_threshold")), current.threshold(),
                        DeepDebrisConfig.MIN_Y, DeepDebrisConfig.MAX_Y)
                .setDefaultValue(DeepDebrisConfig.DEFAULT_THRESHOLD)
                .setTooltip(Component.translatable(key("depth_threshold.tooltip")))
                .build();

        var sourceEntry = entryBuilder
                .startEnumSelector(Component.translatable(key("texture_source")), TextureSource.class, current.source())
                .setDefaultValue(DeepDebrisConfig.DEFAULT_SOURCE)
                .setEnumNameProvider(value -> Component.translatable(key("texture_source." + value.name().toLowerCase(Locale.ROOT))))
                .setTooltip(Component.translatable(key("texture_source.tooltip")))
                .build();

        var packEntry = entryBuilder
                .startStringDropdownMenu(Component.translatable(key("custom_pack")), current.customPack(), CustomPacks::displayName)
                .setSelections(CustomPacks.available(current.customPack()))
                .setSuggestionMode(false)
                .setDefaultValue(DeepDebrisConfig.DEFAULT_CUSTOM_PACK)
                .setTooltip(Component.translatable(key("custom_pack.tooltip")))
                .setDisplayRequirement(Requirement.isValue(sourceEntry, TextureSource.CUSTOM))
                .build();

        general.addEntry(enabledEntry);
        general.addEntry(thresholdEntry);
        general.addEntry(sourceEntry);
        general.addEntry(packEntry);

        builder.setSavingRunnable(() -> DeepDebrisConfig.apply(enabledEntry.getValue(), thresholdEntry.getValue(), sourceEntry.getValue(), packEntry.getValue()));
        return builder.build();
    }

    private static String key(String name) {
        return "config." + MOD_ID + "." + name;
    }
}
