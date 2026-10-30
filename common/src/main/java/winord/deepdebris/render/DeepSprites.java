package winord.deepdebris.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import winord.deepdebris.DeepDebris;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class DeepSprites {
    private static final Map<Identifier, Identifier> REPLACEMENTS = Map.of(
            Identifier.withDefaultNamespace("block/ancient_debris_side"),
            Identifier.fromNamespaceAndPath(DeepDebris.MOD_ID, "block/ancient_debris_side"),
            Identifier.withDefaultNamespace("block/ancient_debris_top"),
            Identifier.fromNamespaceAndPath(DeepDebris.MOD_ID, "block/ancient_debris_top"));

    private final Map<Identifier, TextureAtlasSprite> resolved = new ConcurrentHashMap<>();
    private final Set<Identifier> failed = ConcurrentHashMap.newKeySet();

    @Nullable
    public TextureAtlasSprite replacementFor(Identifier original) {
        var target = REPLACEMENTS.get(original);
        if (target == null) return null;

        var cached = resolved.get(original);
        if (cached != null) return cached;
        if (failed.contains(original)) return null;

        var atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
        var sprite = atlas.getSprite(target);
        if (sprite == atlas.missingSprite()) {
            if (failed.add(original)) DeepDebris.LOGGER.warn("Texture {} is missing, {} stays vanilla", target, original);
            return null;
        }

        resolved.put(original, sprite);
        return sprite;
    }
}
