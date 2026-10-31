package winord.deepdebris.render;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.RandomSource;
import winord.deepdebris.config.OreType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DeepVariant {
    private final DeepSprites sprites;
    private final Map<PartKey, BlockStateModelPart> cache = new ConcurrentHashMap<>();
    private final Map<ParticleKey, Material.Baked> particleCache = new ConcurrentHashMap<>();

    public DeepVariant(OreType ore) {
        this.sprites = new DeepSprites(ore);
    }

    public void collectDeepParts(BlockStateModel original, RandomSource random, TextureVariant variant, List<BlockStateModelPart> output) {
        var parts = new ArrayList<BlockStateModelPart>();
        original.collectParts(random, parts);
        for (var part : parts) {
            output.add(cache.computeIfAbsent(new PartKey(variant, part), key -> DeepPartRemapper.remap(key.part(), sprites, variant)));
        }
    }

    public Material.Baked deepParticleMaterial(BlockStateModel original, TextureVariant variant) {
        var base = original.particleMaterial();
        var key = new ParticleKey(variant, base);
        var cached = particleCache.get(key);
        if (cached != null) return cached;

        var sprite = sprites.replacementFor(variant, base.sprite().contents().name());
        if (sprite == null) return base;

        var deep = new Material.Baked(sprite, base.forceTranslucent());
        particleCache.put(key, deep);
        return deep;
    }

    public int deepMaterialFlags(BlockStateModel original, TextureVariant variant) {
        var parts = new ArrayList<BlockStateModelPart>();
        collectDeepParts(original, RandomSource.create(0L), variant, parts);
        var flags = 0;
        for (var part : parts) flags |= part.materialFlags();
        return flags;
    }

    private record PartKey(TextureVariant variant, BlockStateModelPart part) { }

    private record ParticleKey(TextureVariant variant, Material.Baked base) { }
}
