package winord.deepdebris.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.rendering.v1.SpriteSourceRegistry;
import winord.deepdebris.DeepDebrisClient;
import winord.deepdebris.config.OreType;
import winord.deepdebris.render.CustomPackTextureSource;

public final class DeepDebrisFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DeepDebrisClient.initClient();
        SpriteSourceRegistry.register(CustomPackTextureSource.TYPE, CustomPackTextureSource.CODEC);
        ModelLoadingPlugin.register(context -> context.modifyBlockModelAfterBake()
                .register(ModelModifier.WRAP_PHASE, (model, modifierContext) -> {
                    var ore = OreType.of(modifierContext.state());
                    return ore != null ? new DeepDebrisFabricModel(model, ore) : model;
                }));
    }
}
