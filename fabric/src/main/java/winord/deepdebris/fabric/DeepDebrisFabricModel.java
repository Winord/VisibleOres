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
import winord.deepdebris.render.RemapKind;

import java.util.ArrayList;
import java.util.function.Predicate;

public final class DeepDebrisFabricModel extends WrapperBlockStateModel {
    private final DeepVariant variant = new DeepVariant();

    public DeepDebrisFabricModel(BlockStateModel wrapped) {
        super(wrapped);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        var kind = DeepRule.kind(level, pos);
        if (kind == null) {
            super.emitQuads(emitter, level, pos, state, random, cullTest);
            return;
        }

        var parts = new ArrayList<BlockStateModelPart>();
        variant.collectParts(kind, wrapped, random, parts);
        for (var part : parts) part.emitQuads(emitter, cullTest);
    }

    @Override
    @Nullable
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        var key = super.createGeometryKey(level, pos, state, random);
        if (key == null) return null;
        var kind = DeepRule.kind(level, pos);
        return kind == null ? key : new RemapKey(kind, key);
    }

    @Override
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        var kind = DeepRule.kind(level, pos);
        if (kind == null) return super.materialFlags(level, pos, state, random);
        return variant.materialFlags(kind, wrapped);
    }

    @Override
    public boolean hasMaterialFlag(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, int flag) {
        return (materialFlags(level, pos, state, random) & flag) != 0;
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        var kind = DeepRule.kind(level, pos);
        if (kind == null) return super.particleMaterial(level, pos, state);
        return variant.particleMaterial(kind, wrapped);
    }

    private record RemapKey(RemapKind kind, Object inner) { }
}
