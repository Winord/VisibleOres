package winord.visibleores.config.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix3x2f;
import winord.visibleores.render.OrePreviews;

import java.util.List;

final class BlockPreview {
    private static final float COS_30 = 0.8660254F;
    private static final int TOP_SHADE = 0xFFFFFFFF;
    private static final int SOUTH_SHADE = 0xFFCFCFCF;
    private static final int EAST_SHADE = 0xFFA3A3A3;

    private BlockPreview() { }

    static int width(int height) {
        return Math.round(height * COS_30);
    }

    static void draw(GuiGraphicsExtractor graphics, OrePreviews.Cube cube, float x, float y, int height) {
        var edge = height / 2F;
        var dx = edge * COS_30;
        var dy = edge / 2F;
        var centerX = x + dx;

        face(graphics, cube.top(), centerX, y, dx, dy, -dx, dy, TOP_SHADE);
        face(graphics, cube.south(), centerX - dx, y + dy, dx, dy, 0, edge, SOUTH_SHADE);
        face(graphics, cube.east(), centerX, y + edge, dx, -dy, 0, edge, EAST_SHADE);
    }

    private static void face(GuiGraphicsExtractor graphics, List<OrePreviews.Layer> layers,
                             float originX, float originY, float ux, float uy, float vx, float vy, int shade) {
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.mul(new Matrix3x2f(ux / 16F, uy / 16F, vx / 16F, vy / 16F, originX, originY));
        for (var layer : layers) {
            var color = layer.emissive() ? 0xFFFFFFFF : shade;
            if (layer.sprite() != null) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, layer.sprite(), 0, 0, 16, 16, color);
            } else if (layer.texture() != null) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, layer.texture(), 0, 0, 0F, 0F, 16, 16,
                        layer.regionWidth(), layer.regionHeight(), layer.textureWidth(), layer.textureHeight(), color);
            }
        }
        pose.popMatrix();
    }
}
