package winord.deepdebris.render;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import winord.deepdebris.DeepDebris;
import winord.deepdebris.config.DeepDebrisConfig;
import winord.deepdebris.config.TextureSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public record CustomPackTextureSource(Identifier resource, Identifier sprite) implements SpriteSource {
    public static final Identifier TYPE = Identifier.fromNamespaceAndPath(DeepDebris.MOD_ID, "custom_pack_texture");
    public static final MapCodec<CustomPackTextureSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("resource").forGetter(CustomPackTextureSource::resource),
            Identifier.CODEC.fieldOf("sprite").forGetter(CustomPackTextureSource::sprite)
    ).apply(instance, CustomPackTextureSource::new));

    private static volatile String loadedPack = "";

    public static String loadedPack() {
        return loadedPack;
    }

    @Override
    public void run(ResourceManager resourceManager, Output output) {
        var config = DeepDebrisConfig.get();
        var packId = config.source() == TextureSource.CUSTOM ? config.customPack() : "";
        loadedPack = packId;
        if (packId.isEmpty()) return;

        var pack = Minecraft.getInstance().getResourcePackRepository().getPack(packId);
        if (pack == null) {
            DeepDebris.LOGGER.warn("Resource pack {} is not available", packId);
            return;
        }

        var file = TEXTURE_ID_CONVERTER.idToFile(resource);
        var resources = pack.open();
        try {
            var image = resources.getResource(PackType.CLIENT_RESOURCES, file);
            if (image == null) {
                DeepDebris.LOGGER.warn("Resource pack {} has no {}", packId, file);
                return;
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
        } catch (IOException e) {
            DeepDebris.LOGGER.warn("Could not read {} from {}", file, packId, e);
        } finally {
            resources.close();
        }
    }

    @Override
    public MapCodec<CustomPackTextureSource> codec() {
        return CODEC;
    }
}
