package winord.visibleores.render;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;
import winord.visibleores.VisibleOres;
import winord.visibleores.config.ConfigSnapshot;
import winord.visibleores.config.CustomPacks;
import winord.visibleores.config.OreType;
import winord.visibleores.config.VisibleOresConfig;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public record OreSpriteSource() implements SpriteSource {
    public static final Identifier TYPE = Identifier.fromNamespaceAndPath(VisibleOres.MOD_ID, "ore_sprites");
    public static final MapCodec<OreSpriteSource> CODEC = MapCodec.unit(new OreSpriteSource());

    private static volatile Map<OreType, String> loadedPacks = Map.of();

    public static boolean needsReload(ConfigSnapshot snapshot) {
        for (var ore : OreType.values()) {
            var pack = snapshot.ore(ore).selectedPack();
            if (!pack.isEmpty() && !pack.equals(loadedPacks.getOrDefault(ore, ""))) return true;
        }
        return false;
    }

    @Override
    public void run(ResourceManager resourceManager, Output output) {
        var config = VisibleOresConfig.get();
        var repository = Minecraft.getInstance().getResourcePackRepository();
        var activeSuffix = EmissiveSuffix.read(resourceManager);
        var loaded = new EnumMap<OreType, String>(OreType.class);
        var plans = new EnumMap<OreType, PackModelPlan>(OreType.class);

        addBundledEmissives(resourceManager, activeSuffix, output);

        for (var ore : OreType.values()) {
            var packName = config.ore(ore).selectedPack();
            loaded.put(ore, packName);
            if (packName.isEmpty()) continue;

            var plan = importPack(repository, ore, packName, activeSuffix, output);
            if (plan != null) plans.put(ore, plan);
        }

        loadedPacks = Map.copyOf(loaded);
        PackModelPlan.publish(plans);
    }

    @Nullable
    private static PackModelPlan importPack(PackRepository repository, OreType ore, String packName, @Nullable String activeSuffix, Output output) {
        VisibleOres.LOGGER.info("Loading {} from resource pack {}", ore.key(), packName);
        var pack = repository.getPack(CustomPacks.packId(packName));
        if (pack == null) {
            VisibleOres.LOGGER.warn("Resource pack {} is not available, found: {}", packName, CustomPacks.availableNames());
            return null;
        }

        var resources = pack.open();
        try {
            return PackModelLoader.load(ore, packName, resources, activeSuffix, output);
        } catch (IOException | RuntimeException e) {
            VisibleOres.LOGGER.warn("Could not load {} from resource pack {}", ore.key(), packName, e);
            return null;
        } finally {
            resources.close();
        }
    }

    private static void addBundledEmissives(ResourceManager resourceManager, @Nullable String activeSuffix, Output output) {
        if (activeSuffix == null || activeSuffix.equals(EmissiveSuffix.DEFAULT)) return;

        for (var variant : TextureVariant.values()) {
            if (!variant.hasEmissiveSprites()) continue;
            for (var ore : OreType.values()) {
                for (var slot : ore.slots()) {
                    var file = SpriteSource.TEXTURE_ID_CONVERTER.idToFile(variant.spriteId(slot + EmissiveSuffix.DEFAULT));
                    var resource = resourceManager.getResource(file);
                    if (resource.isEmpty()) continue;
                    try {
                        SpriteUpload.add(resource.get(), variant.spriteId(slot + activeSuffix), output);
                    } catch (IOException e) {
                        VisibleOres.LOGGER.warn("Could not read {}", file, e);
                    }
                }
            }
        }
    }

    @Override
    public MapCodec<OreSpriteSource> codec() {
        return CODEC;
    }
}
