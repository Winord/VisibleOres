/*!stonecutter-disabled
package winord.visibleores.neoforge;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import winord.visibleores.config.OreType;
import winord.visibleores.render.OreModelVariants;
import winord.visibleores.render.TextureRule;

import java.util.List;

public final class VisibleOresNeoForgeModel extends DelegateBlockStateModel {
    private final OreType ore;
    private final OreModelVariants variants;

    public VisibleOresNeoForgeModel(BlockStateModel delegate, OreType ore) {
        super(delegate);
        this.ore = ore;
        this.variants = new OreModelVariants(ore);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        var variant = TextureRule.resolve(ore, level, pos);
        if (variant == null) {
            super.collectParts(level, pos, state, random, parts);
            return;
        }
        variants.collectParts(delegate, state, random, variant, parts);
    }

    @Override
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        var variant = TextureRule.resolve(ore, level, pos);
        if (variant == null) return super.materialFlags(level, pos, state);
        return variants.materialFlags(delegate, state, variant);
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        var variant = TextureRule.resolve(ore, level, pos);
        if (variant == null) return super.particleMaterial(level, pos, state);
        return variants.particleMaterial(delegate, state, variant);
    }
}
*/