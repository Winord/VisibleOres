package winord.deepdebris.input;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import winord.deepdebris.DeepDebris;
import winord.deepdebris.config.DeepDebrisConfig;
import winord.deepdebris.config.OreType;

import java.util.HashMap;
import java.util.Map;

public final class KeybindHandler {
    private static final Map<String, Boolean> WAS_DOWN = new HashMap<>();
    private static final Map<String, InputConstants.Key> PARSED = new HashMap<>();

    private KeybindHandler() { }

    public static void register() {
        ClientTickEvent.CLIENT_POST.register(KeybindHandler::tick);
    }

    public static InputConstants.Key parse(String name) {
        if (name == null || name.isEmpty()) return InputConstants.UNKNOWN;
        return PARSED.computeIfAbsent(name, KeybindHandler::parseUncached);
    }

    public static String format(InputConstants.Key key) {
        return key.equals(InputConstants.UNKNOWN) ? "" : key.getName();
    }

    private static InputConstants.Key parseUncached(String name) {
        try {
            return InputConstants.getKey(name);
        } catch (IllegalArgumentException e) {
            DeepDebris.LOGGER.warn("Unknown key name {}", name);
            return InputConstants.UNKNOWN;
        }
    }

    private static void tick(Minecraft minecraft) {
        var snapshot = DeepDebrisConfig.get();
        var active = minecraft.level != null && minecraft.screen == null;
        poll(minecraft, snapshot, DeepDebrisConfig.MASTER_KEY, null, active);
        for (var ore : OreType.values()) poll(minecraft, snapshot, ore.key(), ore, active);
    }

    private static void poll(Minecraft minecraft, DeepDebrisConfig.Snapshot snapshot, String id, OreType ore, boolean active) {
        var key = parse(snapshot.keybind(id));
        var pressed = !key.equals(InputConstants.UNKNOWN)
                && key.getType() == InputConstants.Type.KEYSYM
                && InputConstants.isKeyDown(minecraft.getWindow(), key.getValue());
        var wasDown = WAS_DOWN.getOrDefault(id, false);
        WAS_DOWN.put(id, pressed);
        if (!pressed || wasDown || !active) return;

        var state = ore == null ? DeepDebrisConfig.toggleMaster() : DeepDebrisConfig.toggleOre(ore);
        var stateText = Component.translatable("config." + DeepDebris.MOD_ID + (state ? ".message.on" : ".message.off"));
        var message = ore == null
                ? Component.translatable("config." + DeepDebris.MOD_ID + ".message.master", stateText)
                : Component.translatable("config." + DeepDebris.MOD_ID + ".message.ore", ore.displayName(), stateText);
        minecraft.gui.setOverlayMessage(message, false);
    }
}
