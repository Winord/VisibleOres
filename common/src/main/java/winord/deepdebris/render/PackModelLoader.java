package winord.deepdebris.render;

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
import winord.deepdebris.DeepDebris;
import winord.deepdebris.config.OreType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PackModelLoader {
    private PackModelLoader() { }

    @Nullable
    public static PackModelPlan load(OreType ore, String packName, PackResources pack, @Nullable String activeSuffix, SpriteSource.Output output) throws IOException {
        var stack = new MultiPackResourceManager(PackType.CLIENT_RESOURCES, List.of(Minecraft.getInstance().getVanillaPackResources(), pack));
        var packId = pack.packId();
        var usesPack = false;

        var block = BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(ore.key()));
        var stateFile = Identifier.withDefaultNamespace("blockstates/" + ore.key() + ".json");
        var stateResource = stack.getResource(stateFile);
        if (stateResource.isEmpty()) {
            DeepDebris.LOGGER.warn("No blockstate {} for {}", stateFile, ore.key());
            return null;
        }
        usesPack |= packId.equals(stateResource.get().sourcePackId());

        Map<BlockState, BlockStateModel.UnbakedRoot> roots;
        try (var reader = stateResource.get().openAsReader()) {
            var definition = BlockStateModelDispatcher.CODEC.parse(JsonOps.INSTANCE, StrictJsonParser.parse(reader)).getOrThrow(JsonParseException::new);
            roots = definition.instantiate(block.getStateDefinition(), () -> stateFile + "/" + packName);
        } catch (RuntimeException e) {
            DeepDebris.LOGGER.warn("Could not read blockstate {} from {}", stateFile, packName, e);
            return null;
        }

        var uniqueRoots = Collections.<BlockStateModel.UnbakedRoot>newSetFromMap(new IdentityHashMap<>());
        uniqueRoots.addAll(roots.values());

        var seen = new HashSet<Identifier>();
        var pending = new ArrayDeque<Identifier>();
        ResolvableModel.Resolver collector = id -> {
            if (seen.add(id)) pending.add(id);
        };
        for (var root : uniqueRoots) root.resolveDependencies(collector);

        var rawModels = new HashMap<Identifier, UnbakedModel>();
        while (!pending.isEmpty()) {
            var id = pending.poll();
            var file = id.withPath("models/" + id.getPath() + ".json");
            var resource = stack.getResource(file);
            if (resource.isEmpty()) continue;
            usesPack |= packId.equals(resource.get().sourcePackId());
            try (var reader = resource.get().openAsReader()) {
                var model = CuboidModel.fromStream(reader);
                rawModels.put(id, model);
                var parent = model.parent();
                if (parent != null && seen.add(parent)) pending.add(parent);
            } catch (RuntimeException e) {
                DeepDebris.LOGGER.warn("Could not read model {} from {}", file, packName, e);
                return null;
            }
        }

        var discovery = new ModelDiscovery(rawModels, MissingCuboidModel.missingModel());
        for (var root : uniqueRoots) discovery.addRoot(root);
        var resolved = discovery.resolve();
        var missing = discovery.missingModel();

        var textures = new LinkedHashSet<Identifier>();
        for (var top : resolved.values()) {
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

        var packSuffix = EmissiveSuffix.of(stack);
        if (packSuffix == null) packSuffix = EmissiveSuffix.DEFAULT;

        var sprites = new HashMap<Identifier, Identifier>();
        for (var texture : textures) {
            var base = stack.getResource(SpriteSource.TEXTURE_ID_CONVERTER.idToFile(texture));
            var emissive = texture.getPath().endsWith(packSuffix)
                    ? Optional.<Resource>empty()
                    : stack.getResource(SpriteSource.TEXTURE_ID_CONVERTER.idToFile(texture.withPath(texture.getPath() + packSuffix)));
            var baseFromPack = base.isPresent() && packId.equals(base.get().sourcePackId());
            var emissiveFromPack = emissive.isPresent() && packId.equals(emissive.get().sourcePackId());
            if (base.isEmpty() || !(baseFromPack || emissiveFromPack)) {
                sprites.put(texture, texture);
                continue;
            }

            usesPack = true;
            var spriteId = Identifier.fromNamespaceAndPath(DeepDebris.MOD_ID,
                    "block/custom/" + ore.key() + "/" + texture.getNamespace() + "/" + texture.getPath());
            add(base.get(), spriteId, output);
            DeepDebris.LOGGER.info("Using {} from {} as {}", texture, baseFromPack ? packName : "vanilla", spriteId);
            if (emissiveFromPack) {
                if (activeSuffix == null) {
                    DeepDebris.LOGGER.warn("Emissive texture of {} is ignored: no emissive suffix is active", texture);
                } else {
                    var emissiveId = spriteId.withPath(spriteId.getPath() + activeSuffix);
                    add(emissive.get(), emissiveId, output);
                    DeepDebris.LOGGER.info("Using emissive {} from {} as {}", texture.withPath(texture.getPath() + packSuffix), packName, emissiveId);
                }
            }
            sprites.put(texture, spriteId);
        }

        if (!usesPack) {
            DeepDebris.LOGGER.warn("Resource pack {} changes nothing for {}", packName, ore.key());
            return null;
        }
        return new PackModelPlan(ore, roots, resolved, missing, sprites);
    }

    public static void add(Resource resource, Identifier id, SpriteSource.Output output) throws IOException {
        byte[] bytes;
        try (var in = resource.open()) {
            bytes = in.readAllBytes();
        }
        var metadata = resource.metadata();
        warnOddSize(id, bytes);
        output.add(id, new Resource(resource.source(), () -> new ByteArrayInputStream(bytes), () -> metadata));
    }

    private static void warnOddSize(Identifier id, byte[] png) {
        if (png.length < 24) return;
        var width = ((png[16] & 0xFF) << 24) | ((png[17] & 0xFF) << 16) | ((png[18] & 0xFF) << 8) | (png[19] & 0xFF);
        if (width > 0 && (width & (width - 1)) != 0) {
            DeepDebris.LOGGER.warn("Texture {} is {} pixels wide, not a power of two: the block atlas mipmap level will be lowered", id, width);
        }
    }
}
