package winord.visibleores.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import winord.visibleores.render.WaxedEntityTextures;
import winord.visibleores.render.WaxedRenderState;

/** Waxed copper chests use the waxed chest textures when Waxed Copper is applied to blocks. */
@Mixin(ChestRenderer.class)
public abstract class ChestRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/client/renderer/blockentity/state/ChestRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
            at = @At("TAIL"))
    private void visibleores$markWaxed(BlockEntity blockEntity, ChestRenderState state, float partialTick, Vec3 camera,
                                       ModelFeatureRenderer.@Nullable CrumblingOverlay overlay, CallbackInfo ci) {
        ((WaxedRenderState) state).visibleores$setWaxed(WaxedEntityTextures.applies(blockEntity));
    }

    @Redirect(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/ChestRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Sheets;chooseSprite(Lnet/minecraft/client/renderer/blockentity/state/ChestRenderState$ChestMaterialType;Lnet/minecraft/world/level/block/state/properties/ChestType;)Lnet/minecraft/client/resources/model/sprite/SpriteId;"))
    private SpriteId visibleores$waxedSprite(ChestRenderState.ChestMaterialType material, ChestType type,
                                             ChestRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        var sprite = Sheets.chooseSprite(material, type);
        return ((WaxedRenderState) state).visibleores$waxed() ? WaxedEntityTextures.chest(material, type, sprite) : sprite;
    }
}
