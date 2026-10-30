package winord.deepdebris.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.rendering.v1.SpriteSourceRegistry;
import net.minecraft.world.level.block.Blocks;
import winord.deepdebris.DeepDebrisClient;
import winord.deepdebris.render.CustomPackTextureSource;

public final class DeepDebrisFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DeepDebrisClient.initClient();
        SpriteSourceRegistry.register(CustomPackTextureSource.TYPE, CustomPackTextureSource.CODEC);
        ModelLoadingPlugin.register(context -> context.modifyBlockModelAfterBake()
                .register(ModelModifier.WRAP_PHASE, (model, modifierContext) ->
                        modifierContext.state().is(Blocks.ANCIENT_DEBRIS) ? new DeepDebrisFabricModel(model) : model));
    }
}
