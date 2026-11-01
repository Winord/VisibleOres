package winord.visibleores.render;

import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import winord.visibleores.VisibleOres;

import java.io.ByteArrayInputStream;
import java.io.IOException;

final class SpriteUpload {
    private SpriteUpload() { }

    static void add(Resource resource, Identifier id, SpriteSource.Output output) throws IOException {
        byte[] bytes;
        try (var in = resource.open()) {
            bytes = in.readAllBytes();
        }
        var metadata = resource.metadata();
        warnIfNotPowerOfTwo(id, bytes);
        output.add(id, new Resource(resource.source(), () -> new ByteArrayInputStream(bytes), () -> metadata));
    }

    private static void warnIfNotPowerOfTwo(Identifier id, byte[] png) {
        if (png.length < 24) return;
        var width = ((png[16] & 0xFF) << 24) | ((png[17] & 0xFF) << 16) | ((png[18] & 0xFF) << 8) | (png[19] & 0xFF);
        if (width > 0 && (width & (width - 1)) != 0) {
            VisibleOres.LOGGER.warn("Texture {} is {} pixels wide, not a power of two: the block atlas mipmap level will be lowered", id, width);
        }
    }
}
