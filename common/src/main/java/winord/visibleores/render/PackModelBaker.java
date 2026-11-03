package winord.visibleores.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import org.joml.Vector3fc;
import winord.visibleores.VisibleOres;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

final class PackModelBaker implements ModelBaker {
    private final PackModelPlan plan;
    private final Object materialsLock = new Object();
    private volatile MaterialBaker materials;
    private final ModelBaker.Interner interner = new IdentityInterner();
    private final Map<ModelBaker.SharedOperationKey<Object>, Object> operations = new ConcurrentHashMap<>();
    private final Set<Identifier> reported = ConcurrentHashMap.newKeySet();

    PackModelBaker(PackModelPlan plan) {
        this.plan = plan;
    }

    @Override
    public ResolvedModel getModel(Identifier location) {
        return plan.resolved(location);
    }

    @Override
    public BlockStateModelPart missingBlockModelPart() {
        var parts = new ArrayList<BlockStateModelPart>();
        Minecraft.getInstance().getModelManager().getBlockStateModelSet().missingModel().collectParts(RandomSource.create(0L), parts);
        return parts.getFirst();
    }

    @Override
    public MaterialBaker materials() {
        var current = materials;
        if (current != null) return current;
        synchronized (materialsLock) {
            if (materials == null) materials = new Materials(atlas().missingSprite());
            return materials;
        }
    }

    @Override
    public ModelBaker.Interner interner() {
        return interner;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T compute(ModelBaker.SharedOperationKey<T> key) {
        return (T) operations.computeIfAbsent((ModelBaker.SharedOperationKey<Object>) key, k -> k.compute(this));
    }

    private static SpriteLoader.Preparations emptyAtlas(TextureAtlasSprite missingSprite) {
        return new SpriteLoader.Preparations(0, 0, 0, missingSprite, Map.of(), CompletableFuture.completedFuture(null));
    }

    private static TextureAtlas atlas() {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
    }

    private final class Materials extends MaterialBaker {
        Materials(TextureAtlasSprite missingSprite) {
            super(emptyAtlas(missingSprite), emptyAtlas(missingSprite));
        }

        @Override
        public Material.Baked get(Material material, ModelDebugName name) {
            var atlas = atlas();
            var id = plan.sprite(material.sprite());
            var sprite = atlas.getSprite(id);
            if (sprite == atlas.missingSprite() && reported.add(id)) {
                VisibleOres.LOGGER.warn("Texture {} of custom pack model {} is missing from the block atlas", id, name.debugName());
            }
            return new Material.Baked(sprite, material.forceTranslucent());
        }

        @Override
        public Material.Baked reportMissingReference(String reference, ModelDebugName name) {
            VisibleOres.LOGGER.warn("Missing texture reference {} in custom pack model {}", reference, name.debugName());
            return new Material.Baked(atlas().missingSprite(), false);
        }
    }

    private static final class IdentityInterner implements ModelBaker.Interner {
        @Override
        public Vector3fc vector(Vector3fc vector) {
            return vector;
        }

        @Override
        public BakedQuad.MaterialInfo materialInfo(BakedQuad.MaterialInfo material) {
            return material;
        }
    }
}
