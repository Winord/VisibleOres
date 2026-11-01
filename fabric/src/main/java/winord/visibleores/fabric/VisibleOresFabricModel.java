package winord.visibleores.fabric;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import winord.visibleores.config.OreType;
import winord.visibleores.render.OreModelVariants;
import winord.visibleores.render.TextureRule;
import winord.visibleores.render.TextureVariant;

import java.util.ArrayList;
import java.util.function.Predicate;

public final class VisibleOresFabricModel extends WrapperBlockStateModel {
    private final OreType ore;
    private final OreModelVariants variants;

    public VisibleOresFabricModel(BlockStateModel wrapped, OreType ore) {
        super(wrapped);
        this.ore = ore;
        this.variants = new OreModelVariants(ore);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        var variant = TextureRule.resolve(ore, level, pos);
        if (variant == null) {
            super.emitQuads(emitter, level, pos, state, random, cullTest);
            return;
        }

        var parts = new ArrayList<BlockStateModelPart>();
        variants.collectParts(wrapped, state, random, variant, parts);
        for (var part : parts) part.emitQuads(emitter, cullTest);
    }

    @Override
    @Nullable
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        var key = super.createGeometryKey(level, pos, state, random);
        if (key == null) return null;
        var variant = TextureRule.resolve(ore, level, pos);
        return variant == null ? key : new VariantKey(key, variant);
    }

    @Override
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        var variant = TextureRule.resolve(ore, level, pos);
        if (variant == null) return super.materialFlags(level, pos, state, random);
        return variants.materialFlags(wrapped, state, variant);
    }

    @Override
    public boolean hasMaterialFlag(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, int flag) {
        return (materialFlags(level, pos, state, random) & flag) != 0;
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        var variant = TextureRule.resolve(ore, level, pos);
        if (variant == null) return super.particleMaterial(level, pos, state);
        return variants.particleMaterial(wrapped, state, variant);
    }

    private record VariantKey(Object inner, TextureVariant variant) { }
}
