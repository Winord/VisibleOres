package winord.visibleores.mixin;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import winord.visibleores.render.WaxedRenderState;

@Mixin(BlockEntityRenderState.class)
public abstract class BlockEntityRenderStateMixin implements WaxedRenderState {
    @Unique
    private boolean visibleores$waxed;

    @Override
    public boolean visibleores$waxed() {
        return visibleores$waxed;
    }

    @Override
    public void visibleores$setWaxed(boolean waxed) {
        visibleores$waxed = waxed;
    }
}
