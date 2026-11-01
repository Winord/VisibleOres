package winord.visibleores.config;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class CustomPacks {
    private static final String FOLDER_PREFIX = "file/";

    private CustomPacks() { }

    public static String packId(String name) {
        return name.startsWith(FOLDER_PREFIX) ? name : FOLDER_PREFIX + name;
    }

    public static List<String> availableNames() {
        var names = new ArrayList<String>();
        for (var pack : Minecraft.getInstance().getResourcePackRepository().getAvailablePacks()) {
            if (pack.getId().startsWith(FOLDER_PREFIX)) names.add(pack.getId().substring(FOLDER_PREFIX.length()));
        }
        return names;
    }

    static List<String> selections(String current) {
        var names = availableNames();
        var normalized = normalize(current);
        if (!normalized.isEmpty() && !names.contains(normalized)) names.add(normalized);
        return names;
    }

    static String normalize(String name) {
        return name.startsWith(FOLDER_PREFIX) ? name.substring(FOLDER_PREFIX.length()) : name;
    }

    static Component displayName(String name) {
        return Component.literal(name);
    }
}
