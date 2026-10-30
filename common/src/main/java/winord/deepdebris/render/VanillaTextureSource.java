package winord.deepdebris.render;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import winord.deepdebris.DeepDebris;

public record VanillaTextureSource(Identifier resource, Identifier sprite) implements SpriteSource {
    public static final Identifier TYPE = Identifier.fromNamespaceAndPath(DeepDebris.MOD_ID, "vanilla_texture");
    public static final MapCodec<VanillaTextureSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("resource").forGetter(VanillaTextureSource::resource),
            Identifier.CODEC.fieldOf("sprite").forGetter(VanillaTextureSource::sprite)
    ).apply(instance, VanillaTextureSource::new));

    @Override
    public void run(ResourceManager resourceManager, Output output) {
        var file = TEXTURE_ID_CONVERTER.idToFile(resource);
        var stack = resourceManager.getResourceStack(file);
        if (stack.isEmpty()) {
            DeepDebris.LOGGER.warn("Missing sprite: {}", file);
            return;
        }
        output.add(sprite, stack.getFirst());
    }

    @Override
    public MapCodec<VanillaTextureSource> codec() {
        return CODEC;
    }
}
