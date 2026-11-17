package winord.visibleores.render;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import winord.visibleores.VisibleOres;
import winord.visibleores.config.OreType;

import java.util.Locale;

/**
 * Waxed copper chests and golem statues are drawn by block entity renderers, not block models, so they get their
 * waxed textures here: entity/chest/waxed_copper[_state][_left|_right] and entity/copper_golem/waxed_copper_golem[_state].
 */
public final class WaxedEntityTextures {
    private WaxedEntityTextures() { }

    public static boolean applies(BlockEntity blockEntity) {
        var id = BuiltInRegistries.BLOCK.getKey(blockEntity.getBlockState().getBlock());
        if (OreType.of(id) != OreType.WAXED_COPPER) return false;
        return TextureRule.resolve(OreType.WAXED_COPPER, blockEntity.getBlockPos().getY()) == TextureVariant.STANDARD;
    }

    public static SpriteId chest(ChestRenderState.ChestMaterialType material, ChestType type, SpriteId vanilla) {
        var state = switch (material) {
            case COPPER_UNAFFECTED -> "";
            case COPPER_EXPOSED -> "_exposed";
            case COPPER_WEATHERED -> "_weathered";
            case COPPER_OXIDIZED -> "_oxidized";
            default -> null;
        };
        if (state == null) return vanilla;
        var half = switch (type) {
            case SINGLE -> "";
            case LEFT -> "_left";
            case RIGHT -> "_right";
        };
        return new SpriteId(Sheets.CHEST_SHEET, Identifier.fromNamespaceAndPath(VisibleOres.MOD_ID, "entity/chest/waxed_copper" + state + half));
    }

    public static Identifier statue(WeatheringCopper.WeatherState state) {
        var suffix = state == WeatheringCopper.WeatherState.UNAFFECTED ? "" : "_" + state.name().toLowerCase(Locale.ROOT);
        return Identifier.fromNamespaceAndPath(VisibleOres.MOD_ID, "textures/entity/copper_golem/waxed_copper_golem" + suffix + ".png");
    }
}
