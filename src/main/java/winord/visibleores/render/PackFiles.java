package winord.visibleores.render;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.jspecify.annotations.Nullable;
import winord.visibleores.VisibleOres;
import winord.visibleores.config.CustomPacks;

import java.io.IOException;

final class PackFiles {
    private PackFiles() { }

    static byte @Nullable [] asset(String packName, Identifier file) {
        var pack = Minecraft.getInstance().getResourcePackRepository().getPack(CustomPacks.packId(packName));
        if (pack == null) return null;
        //? if >=26.3 {
        var layers = pack.open().toList();
        try {
            for (var i = layers.size() - 1; i >= 0; i--) {
                var supplier = layers.get(i).getResource(PackType.CLIENT_RESOURCES, file);
                if (supplier != null) {
                    try (var in = supplier.get()) {
                        return in.readAllBytes();
                    }
                }
            }
            return null;
        } catch (IOException | RuntimeException e) {
            VisibleOres.LOGGER.warn("Could not read {} from resource pack {}", file, packName, e);
            return null;
        } finally {
            layers.forEach(PackResources::close);
        }
        //?} else {
        /*try (var resources = pack.open()) {
            var supplier = resources.getResource(PackType.CLIENT_RESOURCES, file);
            if (supplier == null) return null;
            try (var in = supplier.get()) {
                return in.readAllBytes();
            }
        } catch (IOException | RuntimeException e) {
            VisibleOres.LOGGER.warn("Could not read {} from resource pack {}", file, packName, e);
            return null;
        }
        *///?}
    }

    static byte @Nullable [] root(String packName, String name) {
        var pack = Minecraft.getInstance().getResourcePackRepository().getPack(CustomPacks.packId(packName));
        if (pack == null) return null;
        //? if >=26.3 {
        try (var resources = pack.openMetadata()) {
        //?} else
        //try (var resources = pack.open()) {
            var supplier = resources.getRootResource(name);
            if (supplier == null) return null;
            try (var in = supplier.get()) {
                return in.readAllBytes();
            }
        } catch (IOException | RuntimeException e) {
            VisibleOres.LOGGER.warn("Could not read {} from resource pack {}", name, packName, e);
            return null;
        }
    }
}
