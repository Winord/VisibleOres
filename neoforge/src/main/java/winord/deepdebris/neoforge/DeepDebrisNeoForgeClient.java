package winord.deepdebris.neoforge;

import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import winord.deepdebris.DeepDebrisClient;
import winord.deepdebris.config.ConfigScreens;
import winord.deepdebris.render.CustomPackTextureSource;

final class DeepDebrisNeoForgeClient {
    private DeepDebrisNeoForgeClient() { }

    static void init(ModContainer container) {
        DeepDebrisClient.initClient();
        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, parent) -> ConfigScreens.create(parent));

        var bus = container.getEventBus();
        if (bus != null) {
            bus.addListener(DeepDebrisNeoForgeClient::onModifyBakingResult);
            bus.addListener(DeepDebrisNeoForgeClient::onRegisterSpriteSources);
        }
    }

    private static void onRegisterSpriteSources(RegisterSpriteSourcesEvent event) {
        event.register(CustomPackTextureSource.TYPE, CustomPackTextureSource.CODEC);
    }

    private static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        var models = event.getBakingResult().blockStateModels();
        for (var entry : models.entrySet()) {
            if (entry.getKey().is(Blocks.ANCIENT_DEBRIS)) {
                entry.setValue(new DeepDebrisNeoForgeModel(entry.getValue()));
            }
        }
    }
}
