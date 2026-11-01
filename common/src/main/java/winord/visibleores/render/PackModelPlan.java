package winord.visibleores.render;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import winord.visibleores.VisibleOres;
import winord.visibleores.config.OreType;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

final class PackModelPlan {
    private static volatile Map<OreType, PackModelPlan> plans = Map.of();

    private final OreType ore;
    private final Map<BlockState, BlockStateModel.UnbakedRoot> roots;
    private final Map<Identifier, ResolvedModel> models;
    private final ResolvedModel missing;
    private final Map<Identifier, Identifier> sprites;
    private final PackModelBaker baker = new PackModelBaker(this);
    private final Map<BlockState, Optional<BlockStateModel>> baked = new ConcurrentHashMap<>();

    PackModelPlan(OreType ore, Map<BlockState, BlockStateModel.UnbakedRoot> roots, Map<Identifier, ResolvedModel> models,
                  ResolvedModel missing, Map<Identifier, Identifier> sprites) {
        this.ore = ore;
        this.roots = roots;
        this.models = models;
        this.missing = missing;
        this.sprites = sprites;
    }

    static void publish(Map<OreType, PackModelPlan> next) {
        plans = Map.copyOf(next);
    }

    @Nullable
    static PackModelPlan of(OreType ore) {
        return plans.get(ore);
    }

    @Nullable
    BlockStateModel model(BlockState state) {
        var cached = baked.get(state);
        if (cached == null) cached = baked.computeIfAbsent(state, this::bake);
        return cached.orElse(null);
    }

    ResolvedModel resolved(Identifier id) {
        var model = models.get(id);
        if (model != null) return model;
        VisibleOres.LOGGER.warn("Custom pack model {} of {} was not discovered", id, ore.key());
        return missing;
    }

    Identifier sprite(Identifier texture) {
        return sprites.getOrDefault(texture, texture);
    }

    private Optional<BlockStateModel> bake(BlockState state) {
        var root = roots.get(state);
        if (root == null) return Optional.empty();
        try {
            return Optional.of(root.bake(state, baker));
        } catch (RuntimeException e) {
            VisibleOres.LOGGER.warn("Could not bake custom pack model of {} for {}", ore.key(), state, e);
            return Optional.empty();
        }
    }
}
