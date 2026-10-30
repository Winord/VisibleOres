package winord.deepdebris.render;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DeepVariant {
    private final Instance deep = new Instance(RemapKind.DEEP);
    private final Instance custom = new Instance(RemapKind.CUSTOM);

    public void collectParts(RemapKind kind, BlockStateModel original, RandomSource random, List<BlockStateModelPart> output) {
        instance(kind).collectParts(original, random, output);
    }

    public Material.Baked particleMaterial(RemapKind kind, BlockStateModel original) {
        return instance(kind).particleMaterial(original);
    }

    public int materialFlags(RemapKind kind, BlockStateModel original) {
        return instance(kind).materialFlags(original);
    }

    private Instance instance(RemapKind kind) {
        return kind == RemapKind.DEEP ? deep : custom;
    }

    private static final class Instance {
        private final DeepSprites sprites;
        private final Map<BlockStateModelPart, BlockStateModelPart> cache = new ConcurrentHashMap<>();
        private final Map<Material.Baked, Material.Baked> particleCache = new ConcurrentHashMap<>();

        private Instance(RemapKind kind) {
            this.sprites = new DeepSprites(kind);
        }

        private void collectParts(BlockStateModel original, RandomSource random, List<BlockStateModelPart> output) {
            var parts = new ArrayList<BlockStateModelPart>();
            original.collectParts(random, parts);
            for (var part : parts) {
                output.add(cache.computeIfAbsent(part, key -> DeepPartRemapper.remap(key, sprites)));
            }
        }

        private Material.Baked particleMaterial(BlockStateModel original) {
            var base = original.particleMaterial();
            var cached = particleCache.get(base);
            if (cached != null) return cached;

            var sprite = sprites.replacementFor(base.sprite().contents().name());
            if (sprite == null) return base;

            var remapped = new Material.Baked(sprite, base.forceTranslucent());
            particleCache.put(base, remapped);
            return remapped;
        }

        private int materialFlags(BlockStateModel original) {
            var parts = new ArrayList<BlockStateModelPart>();
            collectParts(original, RandomSource.create(0L), parts);
            var flags = 0;
            for (var part : parts) flags |= part.materialFlags();
            return flags;
        }
    }
}
