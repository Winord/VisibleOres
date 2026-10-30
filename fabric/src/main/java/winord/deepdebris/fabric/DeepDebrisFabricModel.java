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
import winord.deepdebris.render.DeepRule;
import winord.deepdebris.render.DeepVariant;

import java.util.ArrayList;
import java.util.function.Predicate;

public final class DeepDebrisFabricModel extends WrapperBlockStateModel {
    private final DeepVariant variant = new DeepVariant();

    public DeepDebrisFabricModel(BlockStateModel wrapped) {
        super(wrapped);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        if (!DeepRule.applies(level, pos)) {
            super.emitQuads(emitter, level, pos, state, random, cullTest);
            return;
        }

        var parts = new ArrayList<BlockStateModelPart>();
        variant.collectDeepParts(wrapped, random, parts);
        for (var part : parts) part.emitQuads(emitter, cullTest);
    }

    @Override
    @Nullable
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        var key = super.createGeometryKey(level, pos, state, random);
        if (key == null || !DeepRule.applies(level, pos)) return key;
        return new DeepKey(key);
    }

    @Override
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        if (!DeepRule.applies(level, pos)) return super.materialFlags(level, pos, state, random);
        return variant.deepMaterialFlags(wrapped);
    }

    @Override
    public boolean hasMaterialFlag(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, int flag) {
        return (materialFlags(level, pos, state, random) & flag) != 0;
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        if (!DeepRule.applies(level, pos)) return super.particleMaterial(level, pos, state);
        return variant.deepParticleMaterial(wrapped);
    }

    private record DeepKey(Object inner) { }
}
