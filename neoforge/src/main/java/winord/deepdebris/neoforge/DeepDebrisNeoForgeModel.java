package winord.deepdebris.neoforge;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import winord.deepdebris.config.OreType;
import winord.deepdebris.render.DeepRule;
import winord.deepdebris.render.DeepVariant;

import java.util.List;

public final class DeepDebrisNeoForgeModel extends DelegateBlockStateModel {
    private final OreType ore;
    private final DeepVariant variant;

    public DeepDebrisNeoForgeModel(BlockStateModel delegate, OreType ore) {
        super(delegate);
        this.ore = ore;
        this.variant = new DeepVariant(ore);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        var texture = DeepRule.resolve(ore, level, pos);
        if (texture == null) {
            super.collectParts(level, pos, state, random, parts);
            return;
        }
        variant.collectDeepParts(delegate, random, texture, parts);
    }

    @Override
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        var texture = DeepRule.resolve(ore, level, pos);
        if (texture == null) return super.materialFlags(level, pos, state);
        return variant.deepMaterialFlags(delegate, texture);
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        var texture = DeepRule.resolve(ore, level, pos);
        if (texture == null) return super.particleMaterial(level, pos, state);
        return variant.deepParticleMaterial(delegate, texture);
    }
}
