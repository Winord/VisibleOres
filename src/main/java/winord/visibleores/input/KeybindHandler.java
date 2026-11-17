package winord.visibleores.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import winord.visibleores.VisibleOres;
import winord.visibleores.config.ConfigSnapshot;
import winord.visibleores.config.OreType;
import winord.visibleores.config.VisibleOresConfig;
import winord.visibleores.util.Lang;

import java.util.HashMap;
import java.util.Map;

public final class KeybindHandler {
    private static final Map<String, Boolean> WAS_DOWN = new HashMap<>();
    private static final Map<String, InputConstants.Key> PARSED = new HashMap<>();

    private KeybindHandler() { }

    public static InputConstants.Key parse(String name) {
        if (name.isEmpty()) return InputConstants.UNKNOWN;
        return PARSED.computeIfAbsent(name, KeybindHandler::parseUncached);
    }

    public static String format(InputConstants.Key key) {
        return key.equals(InputConstants.UNKNOWN) ? "" : key.getName();
    }

    private static InputConstants.Key parseUncached(String name) {
        try {
            return InputConstants.getKey(name);
        } catch (IllegalArgumentException e) {
            VisibleOres.LOGGER.warn("Unknown key name {}", name);
            return InputConstants.UNKNOWN;
        }
    }

    public static void tick(Minecraft minecraft) {
        var snapshot = VisibleOresConfig.get();
        var active = minecraft.level != null && minecraft.gui.screen() == null;

        if (justPressed(minecraft, snapshot, ConfigSnapshot.MASTER_KEY) && active) {
            var enabled = VisibleOresConfig.toggleMaster();
            show(minecraft, Lang.message("master", state(enabled)));
        }
        for (var ore : OreType.values()) {
            if (justPressed(minecraft, snapshot, ore.key()) && active) {
                var enabled = VisibleOresConfig.toggleOre(ore);
                show(minecraft, Lang.message("ore", ore.displayName(), state(enabled)));
            }
        }
    }

    private static boolean justPressed(Minecraft minecraft, ConfigSnapshot snapshot, String id) {
        var key = parse(snapshot.keybind(id));
        var pressed = !key.equals(InputConstants.UNKNOWN)
                && key.getType() == InputConstants.Type.KEYBOARD
                && InputConstants.isKeyDown(key.getValue());
        var wasDown = WAS_DOWN.getOrDefault(id, false);
        WAS_DOWN.put(id, pressed);
        return pressed && !wasDown;
    }

    private static Component state(boolean enabled) {
        return Lang.message(enabled ? "on" : "off");
    }

    private static void show(Minecraft minecraft, Component message) {
        minecraft.gui.hud.setOverlayMessage(message, false);
    }
}
