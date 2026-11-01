package winord.visibleores.neoforge;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import winord.visibleores.VisibleOresClient;
import winord.visibleores.config.ConfigScreens;
import winord.visibleores.config.OreType;
import winord.visibleores.render.OreSpriteSource;

final class VisibleOresNeoForgeClient {
    private VisibleOresNeoForgeClient() { }

    static void init(ModContainer container) {
        VisibleOresClient.initClient();
        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, parent) -> ConfigScreens.create(parent));

        var bus = container.getEventBus();
        if (bus != null) {
            bus.addListener(VisibleOresNeoForgeClient::onModifyBakingResult);
            bus.addListener(VisibleOresNeoForgeClient::onRegisterSpriteSources);
        }
    }

    private static void onRegisterSpriteSources(RegisterSpriteSourcesEvent event) {
        event.register(OreSpriteSource.TYPE, OreSpriteSource.CODEC);
    }

    private static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        var models = event.getBakingResult().blockStateModels();
        for (var entry : models.entrySet()) {
            var ore = OreType.of(entry.getKey());
            if (ore != null) {
                entry.setValue(new VisibleOresNeoForgeModel(entry.getValue(), ore));
            }
        }
    }
}
