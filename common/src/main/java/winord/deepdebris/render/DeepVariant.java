package winord.deepdebris.render;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import winord.deepdebris.config.OreType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DeepVariant {
    private final OreType ore;
    private final DeepSprites sprites;
    private final Map<PartKey, BlockStateModelPart> cache = new ConcurrentHashMap<>();
    private final Map<ParticleKey, Material.Baked> particleCache = new ConcurrentHashMap<>();

    public DeepVariant(OreType ore) {
        this.ore = ore;
        this.sprites = new DeepSprites(ore);
    }

    public void collectDeepParts(BlockStateModel original, BlockState state, RandomSource random, TextureVariant variant, List<BlockStateModelPart> output) {
        var custom = custom(variant, state);
        if (custom != null) {
            custom.collectParts(random, output);
            return;
        }

        var effective = withoutCustom(variant);
        var parts = new ArrayList<BlockStateModelPart>();
        original.collectParts(random, parts);
        for (var part : parts) {
            output.add(cache.computeIfAbsent(new PartKey(effective, part), key -> DeepPartRemapper.remap(key.part(), sprites, effective)));
        }
    }

    public Material.Baked deepParticleMaterial(BlockStateModel original, BlockState state, TextureVariant variant) {
        var custom = custom(variant, state);
        if (custom != null) return custom.particleMaterial();

        var effective = withoutCustom(variant);
        var base = original.particleMaterial();
        var key = new ParticleKey(effective, base);
        var cached = particleCache.get(key);
        if (cached != null) return cached;

        var sprite = sprites.replacementFor(effective, base.sprite().contents().name());
        if (sprite == null) return base;

        var deep = new Material.Baked(sprite, base.forceTranslucent());
        particleCache.put(key, deep);
        return deep;
    }

    public int deepMaterialFlags(BlockStateModel original, BlockState state, TextureVariant variant) {
        var parts = new ArrayList<BlockStateModelPart>();
        collectDeepParts(original, state, RandomSource.create(0L), variant, parts);
        var flags = 0;
        for (var part : parts) flags |= part.materialFlags();
        return flags;
    }

    @Nullable
    private BlockStateModel custom(TextureVariant variant, BlockState state) {
        if (variant != TextureVariant.CUSTOM) return null;
        var plan = PackModelPlan.of(ore);
        return plan == null ? null : plan.model(state);
    }

    private static TextureVariant withoutCustom(TextureVariant variant) {
        return variant == TextureVariant.CUSTOM ? TextureVariant.STANDARD : variant;
    }

    private record PartKey(TextureVariant variant, BlockStateModelPart part) { }

    private record ParticleKey(TextureVariant variant, Material.Baked base) { }
}
