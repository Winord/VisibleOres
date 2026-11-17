package winord.visibleores.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.CopperGolemStatueBlockRenderer;
import net.minecraft.client.renderer.blockentity.state.CopperGolemStatueRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.golem.CopperGolemOxidationLevel;
import net.minecraft.world.level.block.entity.CopperGolemStatueBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import winord.visibleores.render.WaxedEntityTextures;
import winord.visibleores.render.WaxedRenderState;

/** Waxed copper golem statues use the waxed golem textures when Waxed Copper is applied to blocks. */
@Mixin(CopperGolemStatueBlockRenderer.class)
public abstract class CopperGolemStatueBlockRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/level/block/entity/CopperGolemStatueBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/CopperGolemStatueRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
            at = @At("TAIL"))
    private void visibleores$markWaxed(CopperGolemStatueBlockEntity blockEntity, CopperGolemStatueRenderState state, float partialTick,
                                       Vec3 camera, ModelFeatureRenderer.@Nullable CrumblingOverlay overlay, CallbackInfo ci) {
        ((WaxedRenderState) state).visibleores$setWaxed(WaxedEntityTextures.applies(blockEntity));
    }

    @Redirect(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/CopperGolemStatueRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/golem/CopperGolemOxidationLevel;texture()Lnet/minecraft/resources/Identifier;"))
    private Identifier visibleores$waxedTexture(CopperGolemOxidationLevel level, CopperGolemStatueRenderState state,
                                                PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        return ((WaxedRenderState) state).visibleores$waxed() ? WaxedEntityTextures.statue(state.oxidationState) : level.texture();
    }
}
