package winord.visibleores.render;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;
import winord.visibleores.VisibleOres;
import winord.visibleores.config.OreSettings;
import winord.visibleores.config.OreType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Builds the faces of an ore block exactly as the world renderer would show them, for the config screens.
 * Bundled variants and already loaded custom packs come from the real baked model parts; a custom pack that is
 * not loaded yet is read straight from its files (first animation frame, vanilla model shape).
 * Owns the preview textures it uploads, so {@link #close()} must be called when the screens are gone.
 */
public final class OrePreviews implements AutoCloseable {
    private static final String EMISSIVE_SUFFIX = "_e";

    /** One drawable layer of a face: an atlas sprite, or a standalone texture region. */
    public record Layer(@Nullable TextureAtlasSprite sprite, @Nullable Identifier texture,
                        int regionWidth, int regionHeight, int textureWidth, int textureHeight, boolean emissive) {
        static Layer of(TextureAtlasSprite sprite, boolean emissive) {
            return new Layer(sprite, null, 0, 0, 0, 0, emissive);
        }
    }

    /** Visible faces of a block seen from above, south-east. */
    public record Cube(List<Layer> top, List<Layer> south, List<Layer> east, boolean approximate) { }

    public record Icon(Identifier texture, int width, int height) { }

    private record Key(OreType ore, @Nullable TextureVariant variant, String pack) { }

    private final Map<OreType, OreModelVariants> variants = new EnumMap<>(OreType.class);
    private final Map<Key, Cube> cubes = new HashMap<>();
    private final Map<String, Optional<Layer>> packTextures = new HashMap<>();
    private final Map<String, Boolean> packChanges = new HashMap<>();
    private final Map<String, Optional<Icon>> icons = new HashMap<>();
    private final List<Identifier> uploaded = new ArrayList<>();

    /** The block as it looks in game with these settings. */
    public Cube cube(OreType ore, OreSettings settings, boolean masterEnabled) {
        if (!masterEnabled || !settings.enabled() || !settings.apply().blocks()) return vanilla(ore);
        var variant = TextureVariant.of(settings);
        if (variant == TextureVariant.CUSTOM) return pack(ore, settings.customPack());
        return cubes.computeIfAbsent(new Key(ore, variant, ""), key -> fromModel(ore, variant));
    }

    public Cube vanilla(OreType ore) {
        return cubes.computeIfAbsent(new Key(ore, null, ""), key -> fromModel(ore, null));
    }

    /** The block with the given custom resource pack. */
    public Cube pack(OreType ore, String pack) {
        if (pack.isEmpty()) return vanilla(ore);
        if (pack.equals(OreSpriteSource.loadedPack(ore)) && PackModelPlan.of(ore) != null) {
            return cubes.computeIfAbsent(new Key(ore, TextureVariant.CUSTOM, pack), key -> fromModel(ore, TextureVariant.CUSTOM));
        }
        return cubes.computeIfAbsent(new Key(ore, null, pack), key -> fromPackFiles(ore, pack));
    }

    /** Whether the pack has a blockstate or a texture for this ore. */
    public boolean changes(OreType ore, String pack) {
        if (pack.isEmpty()) return false;
        return packChanges.computeIfAbsent(ore.key() + "|" + pack, key -> {
            for (var block : ore.blocks()) {
                var blockstate = Identifier.withDefaultNamespace("blockstates/" + BuiltInRegistries.BLOCK.getKey(block).getPath() + ".json");
                if (PackFiles.asset(pack, blockstate) != null) return true;
            }
            for (var slot : vanillaTextures(ore)) {
                if (PackFiles.asset(pack, textureFile(slot)) != null) return true;
            }
            return false;
        });
    }

    @Nullable
    public Icon packIcon(String pack) {
        return icons.computeIfAbsent(pack, name -> {
            var bytes = PackFiles.root(name, "pack.png");
            if (bytes == null) return Optional.empty();
            var image = readImage(bytes, name + "/pack.png");
            if (image == null) return Optional.empty();
            var width = image.getWidth();
            var height = image.getHeight();
            return Optional.of(new Icon(upload(image), width, height));
        }).orElse(null);
    }

    @Override
    public void close() {
        var textures = Minecraft.getInstance().getTextureManager();
        for (var id : uploaded) textures.release(id);
        uploaded.clear();
        cubes.clear();
        packTextures.clear();
        icons.clear();
    }

    private Cube fromModel(OreType ore, @Nullable TextureVariant variant) {
        var parts = parts(ore, variant);
        var atlas = atlas();
        return new Cube(layers(parts, Direction.UP, atlas), layers(parts, Direction.SOUTH, atlas), layers(parts, Direction.EAST, atlas), false);
    }

    private Cube fromPackFiles(OreType ore, String pack) {
        var parts = parts(ore, null);
        var atlas = atlas();
        return new Cube(packLayers(parts, Direction.UP, atlas, pack), packLayers(parts, Direction.SOUTH, atlas, pack),
                packLayers(parts, Direction.EAST, atlas, pack), true);
    }

    private List<BlockStateModelPart> parts(OreType ore, @Nullable TextureVariant variant) {
        var state = ore.iconBlock().defaultBlockState();
        var model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
        var parts = new ArrayList<BlockStateModelPart>();
        var random = RandomSource.create(42L);
        if (variant == null) {
            model.collectParts(random, parts);
        } else {
            variants.computeIfAbsent(ore, OreModelVariants::new).collectParts(model, state, random, variant, parts);
        }
        return parts;
    }

    private static List<Layer> layers(List<BlockStateModelPart> parts, Direction direction, TextureAtlas atlas) {
        var layers = new ArrayList<Layer>();
        for (var part : parts) {
            for (var quad : part.getQuads(direction)) {
                var sprite = quad.materialInfo().sprite();
                layers.add(Layer.of(sprite, false));
                var name = sprite.contents().name();
                var emissive = atlas.getSprite(name.withPath(name.getPath() + EMISSIVE_SUFFIX));
                if (emissive != atlas.missingSprite()) layers.add(Layer.of(emissive, true));
            }
        }
        return layers;
    }

    private List<Layer> packLayers(List<BlockStateModelPart> parts, Direction direction, TextureAtlas atlas, String pack) {
        var layers = new ArrayList<Layer>();
        for (var part : parts) {
            for (var quad : part.getQuads(direction)) {
                var sprite = quad.materialInfo().sprite();
                var name = sprite.contents().name();
                var replaced = packTexture(pack, name);
                layers.add(replaced != null ? replaced : Layer.of(sprite, false));
                var emissive = packTexture(pack, name.withPath(name.getPath() + EMISSIVE_SUFFIX));
                if (emissive != null) layers.add(new Layer(null, emissive.texture(), emissive.regionWidth(), emissive.regionHeight(),
                        emissive.textureWidth(), emissive.textureHeight(), true));
            }
        }
        return layers;
    }

    @Nullable
    private Layer packTexture(String pack, Identifier texture) {
        return packTextures.computeIfAbsent(pack + "|" + texture, key -> {
            var bytes = PackFiles.asset(pack, textureFile(texture));
            if (bytes == null) return Optional.empty();
            var image = readImage(bytes, pack + "/" + texture);
            if (image == null) return Optional.empty();
            var width = image.getWidth();
            var height = image.getHeight();
            var frame = Math.min(width, height);
            return Optional.of(new Layer(null, upload(image), width, frame, width, height, false));
        }).orElse(null);
    }

    private List<Identifier> vanillaTextures(OreType ore) {
        var textures = new ArrayList<Identifier>();
        for (var direction : Direction.values()) {
            for (var part : parts(ore, null)) {
                for (var quad : part.getQuads(direction)) {
                    var name = quad.materialInfo().sprite().contents().name();
                    if (!textures.contains(name)) textures.add(name);
                }
            }
        }
        return textures;
    }

    private Identifier upload(NativeImage image) {
        var id = Identifier.fromNamespaceAndPath(VisibleOres.MOD_ID, "config_preview/" + uploaded.size());
        Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(() -> "Visible Ores preview " + id, image));
        uploaded.add(id);
        return id;
    }

    @Nullable
    private static NativeImage readImage(byte[] bytes, String name) {
        try {
            return NativeImage.read(new ByteArrayInputStream(bytes));
        } catch (IOException | RuntimeException e) {
            VisibleOres.LOGGER.warn("Could not read preview image {}", name, e);
            return null;
        }
    }

    private static Identifier textureFile(Identifier texture) {
        return texture.withPath("textures/" + texture.getPath() + ".png");
    }

    private static TextureAtlas atlas() {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
    }
}
