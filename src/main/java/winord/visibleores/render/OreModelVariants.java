package winord.visibleores.render;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import winord.visibleores.config.OreType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class OreModelVariants {
    private final OreType ore;
    private final VariantSprites sprites;
    private final Map<PartKey, BlockStateModelPart> parts = new ConcurrentHashMap<>();
    private final Map<ParticleKey, Material.Baked> particles = new ConcurrentHashMap<>();

    public OreModelVariants(OreType ore) {
        this.ore = ore;
        this.sprites = new VariantSprites(ore);
    }

    public void collectParts(BlockStateModel original, BlockState state, RandomSource random, TextureVariant variant, List<BlockStateModelPart> output) {
        var packModel = packModel(variant, state);
        if (packModel != null) {
            packModel.collectParts(random, output);
            return;
        }

        var bundled = variant.bundled();
        var originalParts = new ArrayList<BlockStateModelPart>();
        original.collectParts(random, originalParts);
        for (var part : originalParts) {
            output.add(parts.computeIfAbsent(new PartKey(bundled, part), key -> PartRemapper.remap(key.part(), sprites, bundled)));
        }
    }

    public Material.Baked particleMaterial(BlockStateModel original, BlockState state, TextureVariant variant) {
        var packModel = packModel(variant, state);
        if (packModel != null) return packModel.particleMaterial();

        var bundled = variant.bundled();
        var base = original.particleMaterial();
        var key = new ParticleKey(bundled, base);
        var cached = particles.get(key);
        if (cached != null) return cached;

        var sprite = sprites.replacementFor(bundled, base.sprite().contents().name());
        if (sprite == null) return base;

        var replaced = new Material.Baked(sprite, base.forceTranslucent());
        particles.put(key, replaced);
        return replaced;
    }

    public int materialFlags(BlockStateModel original, BlockState state, TextureVariant variant) {
        var variantParts = new ArrayList<BlockStateModelPart>();
        collectParts(original, state, RandomSource.create(0L), variant, variantParts);
        var flags = 0;
        for (var part : variantParts) flags |= part.materialFlags();
        return flags;
    }

    @Nullable
    private BlockStateModel packModel(TextureVariant variant, BlockState state) {
        if (variant != TextureVariant.CUSTOM) return null;
        var plan = PackModelPlan.of(ore);
        return plan == null ? null : plan.model(state);
    }

    private record PartKey(TextureVariant variant, BlockStateModelPart part) { }

    private record ParticleKey(TextureVariant variant, Material.Baked base) { }
}
