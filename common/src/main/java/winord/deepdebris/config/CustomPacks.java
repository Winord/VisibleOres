package winord.deepdebris.config;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

import static winord.deepdebris.DeepDebris.MOD_ID;

final class CustomPacks {
    private static final String FOLDER_PREFIX = "file/";

    private CustomPacks() { }

    static List<String> available(String current) {
        var ids = new ArrayList<String>();
        ids.add("");
        for (var pack : Minecraft.getInstance().getResourcePackRepository().getAvailablePacks()) {
            if (pack.getId().startsWith(FOLDER_PREFIX)) ids.add(pack.getId());
        }
        if (!current.isEmpty() && !ids.contains(current)) ids.add(current);
        return ids;
    }

    static Component displayName(String id) {
        if (id.isEmpty()) return Component.translatable("config." + MOD_ID + ".custom_pack.none");
        return Component.literal(id.startsWith(FOLDER_PREFIX) ? id.substring(FOLDER_PREFIX.length()) : id);
    }
}
