package winord.visibleores.render;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.Direction;

final class PartRemapper {
    private PartRemapper() { }

    static BlockStateModelPart remap(BlockStateModelPart part, VariantSprites sprites, TextureVariant variant) {
        if (!(part instanceof SimpleModelWrapper simple)) return part;

        var source = simple.quads();
        var builder = new QuadCollection.Builder();
        var changed = false;

        for (var quad : source.getQuads(null)) {
            var remapped = remapQuad(quad, sprites, variant);
            changed |= remapped != quad;
            builder.addUnculledFace(remapped);
        }
        for (var direction : Direction.values()) {
            for (var quad : source.getQuads(direction)) {
                var remapped = remapQuad(quad, sprites, variant);
                changed |= remapped != quad;
                builder.addCulledFace(direction, remapped);
            }
        }

        if (!changed) return part;
        return new SimpleModelWrapper(builder.build(), simple.useAmbientOcclusion(), simple.particleMaterial());
    }

    private static BakedQuad remapQuad(BakedQuad quad, VariantSprites sprites, TextureVariant variant) {
        var info = quad.materialInfo();
        var original = info.sprite();
        var target = sprites.replacementFor(variant, original.contents().name());
        if (target == null) return quad;

        //? if >=26.3 {
        var targetInfo = new BakedQuad.MaterialInfo(target, info.layer(), info.itemRenderType(),
                info.itemGlintRenderType(), info.itemGlintSpecialRenderType(),
                info.tintIndex(), info.shadeDirectionOverride(), info.lightEmission());
        //?} else {
        /*var targetInfo = new BakedQuad.MaterialInfo(target, info.layer(), info.itemRenderType(),
                info.tintIndex(), info.shade(), info.lightEmission());
        *///?}
        return new BakedQuad(
                quad.position0(), quad.position1(), quad.position2(), quad.position3(),
                remapUv(quad.packedUV0(), original, target),
                remapUv(quad.packedUV1(), original, target),
                remapUv(quad.packedUV2(), original, target),
                remapUv(quad.packedUV3(), original, target),
                quad.direction(), targetInfo);
    }

    private static long remapUv(long packed, TextureAtlasSprite from, TextureAtlasSprite to) {
        var u = fraction(UVPair.unpackU(packed), from.getU0(), from.getU1());
        var v = fraction(UVPair.unpackV(packed), from.getV0(), from.getV1());
        return UVPair.pack(
                to.getU0() + u * (to.getU1() - to.getU0()),
                to.getV0() + v * (to.getV1() - to.getV0()));
    }

    private static float fraction(float value, float min, float max) {
        return (value - min) / (max - min);
    }
}
