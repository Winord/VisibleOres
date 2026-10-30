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
    private final Map<Identifier, Identifier> targets;
    private final Map<Identifier, TextureAtlasSprite> resolved = new ConcurrentHashMap<>();
    private final Set<Identifier> failed = ConcurrentHashMap.newKeySet();

    public DeepSprites(RemapKind kind) {
        this.targets = kind.targets();
    }

    @Nullable
    public TextureAtlasSprite replacementFor(Identifier original) {
        var target = targets.get(original);
        if (target == null) return null;

        var cached = resolved.get(original);
        if (cached != null) return cached;
        if (failed.contains(original)) return null;

        var atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
        var sprite = atlas.getSprite(target);
        if (sprite == atlas.missingSprite()) {
            if (failed.add(original)) DeepDebris.LOGGER.warn("Texture {} is missing, {} stays as is", target, original);
            return null;
        }

        resolved.put(original, sprite);
        return sprite;
    }
}
