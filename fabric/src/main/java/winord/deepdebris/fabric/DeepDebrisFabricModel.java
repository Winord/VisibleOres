package winord.deepdebris.fabric;

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
import winord.deepdebris.config.OreType;
import winord.deepdebris.render.DeepRule;
import winord.deepdebris.render.DeepVariant;
import winord.deepdebris.render.TextureVariant;

import java.util.ArrayList;
import java.util.function.Predicate;

public final class DeepDebrisFabricModel extends WrapperBlockStateModel {
    private final OreType ore;
    private final DeepVariant variant;

    public DeepDebrisFabricModel(BlockStateModel wrapped, OreType ore) {
        super(wrapped);
        this.ore = ore;
        this.variant = new DeepVariant(ore);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        var texture = DeepRule.resolve(ore, level, pos);
        if (texture == null) {
            super.emitQuads(emitter, level, pos, state, random, cullTest);
            return;
        }

        var parts = new ArrayList<BlockStateModelPart>();
        variant.collectDeepParts(wrapped, random, texture, parts);
        for (var part : parts) part.emitQuads(emitter, cullTest);
    }

    @Override
    @Nullable
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        var key = super.createGeometryKey(level, pos, state, random);
        if (key == null) return null;
        var texture = DeepRule.resolve(ore, level, pos);
        return texture == null ? key : new DeepKey(key, texture);
    }

    @Override
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        var texture = DeepRule.resolve(ore, level, pos);
        if (texture == null) return super.materialFlags(level, pos, state, random);
        return variant.deepMaterialFlags(wrapped, texture);
    }

    @Override
    public boolean hasMaterialFlag(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, int flag) {
        return (materialFlags(level, pos, state, random) & flag) != 0;
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        var texture = DeepRule.resolve(ore, level, pos);
        if (texture == null) return super.particleMaterial(level, pos, state);
        return variant.deepParticleMaterial(wrapped, texture);
    }

    private record DeepKey(Object inner, TextureVariant texture) { }
}
