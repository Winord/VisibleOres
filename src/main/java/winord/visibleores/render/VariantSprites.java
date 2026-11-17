package winord.visibleores.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import winord.visibleores.VisibleOres;
import winord.visibleores.config.OreType;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

final class VariantSprites {
    private final OreType ore;
    private final Map<Key, Optional<TextureAtlasSprite>> resolved = new ConcurrentHashMap<>();

    VariantSprites(OreType ore) {
        this.ore = ore;
    }

    @Nullable
    TextureAtlasSprite replacementFor(TextureVariant variant, Identifier original) {
        return resolved.computeIfAbsent(new Key(variant, original), key -> Optional.ofNullable(find(variant, original))).orElse(null);
    }

    @Nullable
    private TextureAtlasSprite find(TextureVariant variant, Identifier original) {
        var slot = replacementOf(original);
        if (slot == null) return null;

        var atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
        var id = variant.spriteId(slot);
        var sprite = atlas.getSprite(id);
        if (sprite == atlas.missingSprite()) {
            VisibleOres.LOGGER.warn("Texture {} is missing, {} stays vanilla", id, original);
            return null;
        }
        return sprite;
    }

    @Nullable
    private String replacementOf(Identifier original) {
        if (!Identifier.DEFAULT_NAMESPACE.equals(original.getNamespace())) return null;
        var path = original.getPath();
        if (!path.startsWith("block/")) return null;
        return ore.textures().get(path.substring("block/".length()));
    }

    private record Key(TextureVariant variant, Identifier original) { }
}
