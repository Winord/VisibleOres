package winord.visibleores.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import winord.visibleores.VisibleOres;
import winord.visibleores.config.ConfigPreview;
import winord.visibleores.config.OreType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class VisibleOresItemModel implements ItemModel {
    private static final Map<Identifier, Boolean> BUNDLED = new ConcurrentHashMap<>();

    private final ItemModel original;
    private final OreType ore;
    private final String item;

    private VisibleOresItemModel(ItemModel original, OreType ore, String item) {
        this.original = original;
        this.ore = ore;
        this.item = item;
    }

    public static ItemModel wrap(Identifier itemId, ItemModel model) {
        var ore = OreType.of(itemId);
        return ore == null ? model : new VisibleOresItemModel(model, ore, itemId.getPath());
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext context,
                       @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        var replacement = replacement();
        (replacement != null ? replacement : original).update(state, stack, resolver, context, level, owner, seed);
    }

    @Nullable
    private ItemModel replacement() {
        var snapshot = ConfigPreview.current();
        if (!snapshot.enabled()) return null;
        var settings = snapshot.ore(ore);
        if (!settings.enabled() || !settings.apply().items()) return null;

        var variant = TextureVariant.of(settings);
        if (variant == TextureVariant.CUSTOM) return null;
        var id = Identifier.fromNamespaceAndPath(VisibleOres.MOD_ID, variant.directory() + "/" + item);
        if (!BUNDLED.computeIfAbsent(id, VisibleOresItemModel::isBundled)) return null;
        return Minecraft.getInstance().getModelManager().getItemModel(id);
    }

    private static boolean isBundled(Identifier id) {
        var file = id.withPath("items/" + id.getPath() + ".json");
        return Minecraft.getInstance().getResourceManager().getResource(file).isPresent();
    }
}
