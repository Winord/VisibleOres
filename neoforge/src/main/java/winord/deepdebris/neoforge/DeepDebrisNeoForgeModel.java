package winord.deepdebris.neoforge;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import winord.deepdebris.render.DeepRule;
import winord.deepdebris.render.DeepVariant;

import java.util.List;

public final class DeepDebrisNeoForgeModel extends DelegateBlockStateModel {
    private final DeepVariant variant = new DeepVariant();

    public DeepDebrisNeoForgeModel(BlockStateModel delegate) {
        super(delegate);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        var kind = DeepRule.kind(level, pos);
        if (kind == null) {
            super.collectParts(level, pos, state, random, parts);
            return;
        }
        variant.collectParts(kind, delegate, random, parts);
    }

    @Override
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        var kind = DeepRule.kind(level, pos);
        if (kind == null) return super.materialFlags(level, pos, state);
        return variant.materialFlags(kind, delegate);
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        var kind = DeepRule.kind(level, pos);
        if (kind == null) return super.particleMaterial(level, pos, state);
        return variant.particleMaterial(kind, delegate);
    }
}
