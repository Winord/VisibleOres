package winord.visibleores.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import winord.visibleores.VisibleOres;

import java.util.Locale;

public final class Lang {
    private Lang() { }

    public static MutableComponent config(String name, Object... args) {
        return Component.translatable("config." + VisibleOres.MOD_ID + "." + name, args);
    }

    public static MutableComponent configEnum(String prefix, Enum<?> value) {
        return config(prefix + "." + value.name().toLowerCase(Locale.ROOT));
    }

    public static MutableComponent message(String name, Object... args) {
        return Component.translatable("message." + VisibleOres.MOD_ID + "." + name, args);
    }
}
