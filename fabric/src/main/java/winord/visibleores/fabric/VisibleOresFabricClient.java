package winord.visibleores.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.rendering.v1.SpriteSourceRegistry;
import winord.visibleores.VisibleOresClient;
import winord.visibleores.config.OreType;
import winord.visibleores.render.OreSpriteSource;

public final class VisibleOresFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        VisibleOresClient.initClient();
        SpriteSourceRegistry.register(OreSpriteSource.TYPE, OreSpriteSource.CODEC);
        ModelLoadingPlugin.register(context -> context.modifyBlockModelAfterBake()
                .register(ModelModifier.WRAP_PHASE, (model, modifierContext) -> {
                    var ore = OreType.of(modifierContext.state());
                    return ore != null ? new VisibleOresFabricModel(model, ore) : model;
                }));
    }
}
