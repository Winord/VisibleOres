package winord.deepdebris.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import winord.deepdebris.DeepDebris;
import winord.deepdebris.config.OreType;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class DeepSprites {
    private final OreType ore;
    private final Map<Key, TextureAtlasSprite> resolved = new ConcurrentHashMap<>();
    private final Set<Key> failed = ConcurrentHashMap.newKeySet();

    public DeepSprites(OreType ore) {
        this.ore = ore;
    }

    @Nullable
    public TextureAtlasSprite replacementFor(TextureVariant variant, Identifier original) {
        var slot = slotOf(original);
        if (slot == null) return null;

        var key = new Key(variant, original);
        var cached = resolved.get(key);
        if (cached != null) return cached;
        if (failed.contains(key)) return null;

        var atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
        var sprite = lookup(atlas, variant, slot);
        if (sprite == null) {
            if (failed.add(key)) DeepDebris.LOGGER.warn("Texture {} is missing, {} stays vanilla", variant.spriteId(slot), original);
            return null;
        }

        resolved.put(key, sprite);
        return sprite;
    }

    @Nullable
    private static TextureAtlasSprite lookup(TextureAtlas atlas, TextureVariant variant, String slot) {
        var sprite = atlas.getSprite(variant.spriteId(slot));
        return sprite == atlas.missingSprite() ? null : sprite;
    }

    @Nullable
    private String slotOf(Identifier original) {
        if (!Identifier.DEFAULT_NAMESPACE.equals(original.getNamespace())) return null;
        var path = original.getPath();
        if (!path.startsWith("block/")) return null;
        var slot = path.substring("block/".length());
        return ore.slots().contains(slot) ? slot : null;
    }

    private record Key(TextureVariant variant, Identifier original) { }
}
