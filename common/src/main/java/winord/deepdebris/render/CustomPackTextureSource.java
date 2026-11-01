package winord.deepdebris.render;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import winord.deepdebris.DeepDebris;
import winord.deepdebris.config.CustomPacks;
import winord.deepdebris.config.DeepDebrisConfig;
import winord.deepdebris.config.OreType;
import winord.deepdebris.config.TextureMode;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public record CustomPackTextureSource() implements SpriteSource {
    public static final Identifier TYPE = Identifier.fromNamespaceAndPath(DeepDebris.MOD_ID, "custom_pack_texture");
    public static final MapCodec<CustomPackTextureSource> CODEC = MapCodec.unit(new CustomPackTextureSource());

    private static final List<String> EMISSIVE_VARIANTS = List.of("fancy", "blue_fancy");

    private static volatile Map<OreType, String> loadedPacks = Map.of();

    public static String loadedPack(OreType ore) {
        return loadedPacks.getOrDefault(ore, "");
    }

    @Override
    public void run(ResourceManager resourceManager, Output output) {
        var config = DeepDebrisConfig.get();
        var repository = Minecraft.getInstance().getResourcePackRepository();
        var loaded = new EnumMap<OreType, String>(OreType.class);
        var plans = new EnumMap<OreType, PackModelPlan>(OreType.class);
        var activeSuffix = EmissiveSuffix.of(resourceManager);
        DeepDebris.LOGGER.info("Custom pack source started, available packs: {}, emissive suffix: {}", CustomPacks.availableNames(), activeSuffix);

        addFancyEmissives(resourceManager, activeSuffix, output);

        for (var ore : OreType.values()) {
            var settings = config.ore(ore);
            var packName = settings.texture() == TextureMode.CUSTOM ? settings.customPack() : "";
            loaded.put(ore, packName);
            if (packName.isEmpty()) continue;
            DeepDebris.LOGGER.info("Loading custom textures of {} from resource pack {}", ore.key(), packName);

            var pack = repository.getPack(CustomPacks.packId(packName));
            if (pack == null) {
                DeepDebris.LOGGER.warn("Resource pack {} is not available, found: {}", packName, CustomPacks.availableNames());
                continue;
            }

            var resources = pack.open();
            try {
                var plan = PackModelLoader.load(ore, packName, resources, activeSuffix, output);
                if (plan != null) plans.put(ore, plan);
            } catch (IOException | RuntimeException e) {
                DeepDebris.LOGGER.warn("Could not load {} from resource pack {}", ore.key(), packName, e);
            } finally {
                resources.close();
            }
        }

        loadedPacks = Map.copyOf(loaded);
        PackModelPlan.publish(plans);
    }

    private static void addFancyEmissives(ResourceManager resourceManager, String activeSuffix, Output output) {
        if (activeSuffix == null || activeSuffix.equals(EmissiveSuffix.DEFAULT)) return;

        for (var directory : EMISSIVE_VARIANTS) {
            for (var ore : OreType.values()) {
                for (var slot : ore.slots()) {
                    var file = Identifier.fromNamespaceAndPath(DeepDebris.MOD_ID, "textures/block/" + directory + "/" + slot + EmissiveSuffix.DEFAULT + ".png");
                    var resource = resourceManager.getResource(file);
                    if (resource.isEmpty()) continue;
                    var id = Identifier.fromNamespaceAndPath(DeepDebris.MOD_ID, "block/" + directory + "/" + slot + activeSuffix);
                    try {
                        PackModelLoader.add(resource.get(), id, output);
                    } catch (IOException e) {
                        DeepDebris.LOGGER.warn("Could not read {}", file, e);
                    }
                }
            }
        }
    }

    @Override
    public MapCodec<CustomPackTextureSource> codec() {
        return CODEC;
    }
}
