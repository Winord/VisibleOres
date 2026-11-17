package winord.visibleores.config;

import net.minecraft.client.gui.screens.Screen;
import winord.visibleores.config.gui.VisibleOresConfigScreen;

public final class ConfigScreens {
    private ConfigScreens() { }

    public static Screen create(Screen parent) {
        return new VisibleOresConfigScreen(parent);
    }
}
