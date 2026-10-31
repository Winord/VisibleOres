package winord.deepdebris.render;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import winord.deepdebris.DeepDebris;
import winord.deepdebris.config.CustomPacks;
import winord.deepdebris.config.DeepDebrisConfig;
import winord.deepdebris.config.OreType;
import winord.deepdebris.config.TextureMode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public record CustomPackTextureSource() implements SpriteSource {
    public static final Identifier TYPE = Identifier.fromNamespaceAndPath(DeepDebris.MOD_ID, "custom_pack_texture");
    public static final MapCodec<CustomPackTextureSource> CODEC = MapCodec.unit(new CustomPackTextureSource());

    private static volatile Map<OreType, String> loadedPacks = Map.of();

    public static String loadedPack(OreType ore) {
        return loadedPacks.getOrDefault(ore, "");
    }

    @Override
    public void run(ResourceManager resourceManager, Output output) {
        var config = DeepDebrisConfig.get();
        var repository = Minecraft.getInstance().getResourcePackRepository();
        var loaded = new EnumMap<OreType, String>(OreType.class);

        for (var ore : OreType.values()) {
            var settings = config.ore(ore);
            var packId = settings.texture() == TextureMode.CUSTOM ? settings.customPack() : "";
            loaded.put(ore, packId);
            if (packId.isEmpty()) continue;

            var pack = repository.getPack(CustomPacks.packId(packId));
            if (pack == null) {
                DeepDebris.LOGGER.warn("Resource pack {} is not available, found: {}", packId, CustomPacks.availableNames());
                continue;
            }

            var resources = pack.open();
            try {
                for (var slot : ore.slots()) {
                    var sprite = TextureVariant.CUSTOM.spriteId(slot);
                    var file = TEXTURE_ID_CONVERTER.idToFile(Identifier.withDefaultNamespace("block/" + slot));
                    try {
                        var image = resources.getResource(PackType.CLIENT_RESOURCES, file);
                        if (image == null) {
                            DeepDebris.LOGGER.warn("Resource pack {} has no {}", packId, file);
                            continue;
                        }

                        byte[] imageBytes;
                        try (var in = image.get()) {
                            imageBytes = in.readAllBytes();
                        }

                        var metadata = ResourceMetadata.EMPTY;
                        var metadataFile = Identifier.fromNamespaceAndPath(file.getNamespace(), file.getPath() + ".mcmeta");
                        var metadataSupplier = resources.getResource(PackType.CLIENT_RESOURCES, metadataFile);
                        if (metadataSupplier != null) {
                            try (var in = metadataSupplier.get()) {
                                metadata = ResourceMetadata.fromJsonStream(in);
                            } catch (Exception e) {
                                DeepDebris.LOGGER.warn("Could not read metadata of {} from {}", file, packId, e);
                            }
                        }

                        var parsed = metadata;
                        output.add(sprite, new Resource(resources, () -> new ByteArrayInputStream(imageBytes), () -> parsed));
                        DeepDebris.LOGGER.info("Using {} from resource pack {} as {}", file, packId, sprite);
                    } catch (IOException e) {
                        DeepDebris.LOGGER.warn("Could not read {} from {}", file, packId, e);
                    }
                }
            } finally {
                resources.close();
            }
        }

        loadedPacks = Map.copyOf(loaded);
    }

    @Override
    public MapCodec<CustomPackTextureSource> codec() {
        return CODEC;
    }
}
