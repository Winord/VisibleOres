package winord.visibleores.render;

import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.resources.model.ModelDiscovery;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.cuboid.MissingCuboidModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import winord.visibleores.VisibleOres;
import winord.visibleores.config.OreType;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

final class PackModelLoader {
    private final OreType ore;
    private final String packName;
    private final Set<String> packIds;
    private final MultiPackResourceManager stack;
    @Nullable
    private final String activeSuffix;
    private final SpriteSource.Output output;
    private boolean usesPack;

    private PackModelLoader(OreType ore, String packName, List<PackResources> packs, @Nullable String activeSuffix, SpriteSource.Output output) {
        this.ore = ore;
        this.packName = packName;
        this.packIds = packs.stream().map(PackResources::packId).collect(Collectors.toUnmodifiableSet());
        var layers = new ArrayList<PackResources>();
        layers.add(Minecraft.getInstance().getVanillaPackResources().fullResources());
        layers.addAll(packs);
        this.stack = new MultiPackResourceManager(PackType.CLIENT_RESOURCES, layers);
        this.activeSuffix = activeSuffix;
        this.output = output;
    }

    @Nullable
    static PackModelPlan load(OreType ore, String packName, List<PackResources> packs, @Nullable String activeSuffix, SpriteSource.Output output) throws IOException {
        return new PackModelLoader(ore, packName, packs, activeSuffix, output).load();
    }

    @Nullable
    private PackModelPlan load() throws IOException {
        var roots = readRoots();
        if (roots == null) return null;

        var uniqueRoots = Collections.<BlockStateModel.UnbakedRoot>newSetFromMap(new IdentityHashMap<>());
        uniqueRoots.addAll(roots.values());
        var rawModels = readModels(uniqueRoots);
        if (rawModels == null) return null;

        var discovery = new ModelDiscovery(rawModels, MissingCuboidModel.missingModel());
        for (var root : uniqueRoots) discovery.addRoot(root);
        var resolved = discovery.resolve();
        var missing = discovery.missingModel();

        var sprites = registerSprites(referencedTextures(resolved.values(), missing));
        if (!usesPack) {
            VisibleOres.LOGGER.warn("Resource pack {} changes nothing for {}", packName, ore.key());
            return null;
        }
        return new PackModelPlan(ore, roots, resolved, missing, sprites);
    }

    @Nullable
    private Map<BlockState, BlockStateModel.UnbakedRoot> readRoots() throws IOException {
        var roots = new HashMap<BlockState, BlockStateModel.UnbakedRoot>();
        for (var block : ore.blocks()) {
            var file = Identifier.withDefaultNamespace("blockstates/" + BuiltInRegistries.BLOCK.getKey(block).getPath() + ".json");
            var resource = stack.getResource(file);
            if (resource.isEmpty()) continue;
            markIfFromPack(resource.get());

            try (var reader = resource.get().openAsReader()) {
                var definition = BlockStateModelDispatcher.CODEC.parse(JsonOps.INSTANCE, StrictJsonParser.parse(reader)).getOrThrow(JsonParseException::new);
                roots.putAll(definition.instantiate(block.getStateDefinition(), () -> file + "/" + packName));
            } catch (RuntimeException e) {
                VisibleOres.LOGGER.warn("Could not read blockstate {} from {}", file, packName, e);
            }
        }
        if (roots.isEmpty()) {
            VisibleOres.LOGGER.warn("No blockstates found for {}", ore.key());
            return null;
        }
        return roots;
    }

    @Nullable
    private Map<Identifier, UnbakedModel> readModels(Collection<BlockStateModel.UnbakedRoot> roots) throws IOException {
        var seen = new HashSet<Identifier>();
        var pending = new ArrayDeque<Identifier>();
        ResolvableModel.Resolver collector = id -> {
            if (seen.add(id)) pending.add(id);
        };
        for (var root : roots) root.resolveDependencies(collector);

        var models = new HashMap<Identifier, UnbakedModel>();
        while (!pending.isEmpty()) {
            var id = pending.poll();
            var file = id.withPath("models/" + id.getPath() + ".json");
            var resource = stack.getResource(file);
            if (resource.isEmpty()) continue;
            markIfFromPack(resource.get());
            try (var reader = resource.get().openAsReader()) {
                var model = CuboidModel.fromStream(reader);
                models.put(id, model);
                var parent = model.parent();
                if (parent != null && seen.add(parent)) pending.add(parent);
            } catch (RuntimeException e) {
                VisibleOres.LOGGER.warn("Could not read model {} from {}", file, packName, e);
                return null;
            }
        }
        return models;
    }

    private static Set<Identifier> referencedTextures(Collection<ResolvedModel> models, ResolvedModel missing) {
        var textures = new LinkedHashSet<Identifier>();
        for (var top : models) {
            if (top == missing) continue;
            var slots = top.getTopTextureSlots();
            var names = new HashSet<String>();
            for (ResolvedModel current = top; current != null; current = current.parent()) {
                names.addAll(current.wrapped().textureSlots().values().keySet());
            }
            for (var name : names) {
                var material = slots.getMaterial(name);
                if (material != null) textures.add(material.sprite());
            }
        }
        return textures;
    }

    private Map<Identifier, Identifier> registerSprites(Set<Identifier> textures) throws IOException {
        var packSuffix = Objects.requireNonNullElse(EmissiveSuffix.read(stack), EmissiveSuffix.DEFAULT);
        var sprites = new HashMap<Identifier, Identifier>();
        for (var texture : textures) sprites.put(texture, registerSprite(texture, packSuffix));
        return sprites;
    }

    private Identifier registerSprite(Identifier texture, String packSuffix) throws IOException {
        var emissiveTexture = texture.withPath(texture.getPath() + packSuffix);
        var base = stack.getResource(SpriteSource.TEXTURE_ID_CONVERTER.idToFile(texture));
        var emissive = texture.getPath().endsWith(packSuffix)
                ? Optional.<Resource>empty()
                : stack.getResource(SpriteSource.TEXTURE_ID_CONVERTER.idToFile(emissiveTexture));
        var baseFromPack = base.isPresent() && isFromPack(base.get());
        var emissiveFromPack = emissive.isPresent() && isFromPack(emissive.get());
        if (base.isEmpty() || !(baseFromPack || emissiveFromPack)) return texture;

        usesPack = true;
        var spriteId = TextureVariant.CUSTOM.spriteId(ore.key() + "/" + texture.getNamespace() + "/" + texture.getPath());
        SpriteUpload.add(base.get(), spriteId, output);
        VisibleOres.LOGGER.info("Using {} from {} as {}", texture, baseFromPack ? packName : "vanilla", spriteId);
        if (emissiveFromPack) registerEmissive(emissive.get(), emissiveTexture, spriteId);
        return spriteId;
    }

    private void registerEmissive(Resource emissive, Identifier emissiveTexture, Identifier spriteId) throws IOException {
        if (activeSuffix == null) {
            VisibleOres.LOGGER.warn("Emissive texture {} is ignored: no emissive suffix is active", emissiveTexture);
            return;
        }
        var emissiveId = spriteId.withPath(spriteId.getPath() + activeSuffix);
        SpriteUpload.add(emissive, emissiveId, output);
        VisibleOres.LOGGER.info("Using emissive {} from {} as {}", emissiveTexture, packName, emissiveId);
    }

    private void markIfFromPack(Resource resource) {
        usesPack |= isFromPack(resource);
    }

    private boolean isFromPack(Resource resource) {
        return packIds.contains(resource.sourcePackId());
    }
}
