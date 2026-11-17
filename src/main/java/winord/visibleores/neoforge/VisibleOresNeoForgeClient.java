/*!stonecutter-disabled
package winord.visibleores.neoforge;

import net.minecraft.client.Minecraft;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import winord.visibleores.VisibleOresClient;
import winord.visibleores.config.ConfigScreens;
import winord.visibleores.config.OreType;
import winord.visibleores.input.KeybindHandler;
import winord.visibleores.render.OreSpriteSource;
import winord.visibleores.render.VisibleOresItemModel;

final class VisibleOresNeoForgeClient {
    private VisibleOresNeoForgeClient() { }

    static void init(ModContainer container) {
        VisibleOresClient.initClient();
        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, parent) -> ConfigScreens.create(parent));

        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> KeybindHandler.tick(Minecraft.getInstance()));

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
        for (var entry : event.getBakingResult().itemStackModels().entrySet()) {
            entry.setValue(VisibleOresItemModel.wrap(entry.getKey(), entry.getValue()));
        }

        var models = event.getBakingResult().blockStateModels();
        for (var entry : models.entrySet()) {
            var ore = OreType.of(entry.getKey());
            if (ore != null) {
                entry.setValue(new VisibleOresNeoForgeModel(entry.getValue(), ore));
            }
        }
    }
}
*/