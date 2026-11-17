package winord.visibleores.config.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import winord.visibleores.config.ApplyMode;
import winord.visibleores.config.ConfigPreview;
import winord.visibleores.config.DimensionFilter;
import winord.visibleores.config.OreSettings;
import winord.visibleores.config.OreType;
import winord.visibleores.config.TextureMode;
import winord.visibleores.render.OrePreviews;
import winord.visibleores.util.Lang;

import java.util.List;
import java.util.function.UnaryOperator;

final class OreSettingsScreen extends Screen {
    private static final int ROW_WIDTH = 310;

    private final Screen parent;
    private final ConfigDraft draft;
    private final OrePreviews previews;
    private final OreType ore;
    private final ItemStack icon;
    private HeaderAndFooterLayout layout;
    private ConfigList list;
    private YSlider minSlider;
    private YSlider maxSlider;

    OreSettingsScreen(Screen parent, ConfigDraft draft, OrePreviews previews, OreType ore) {
        super(ore.displayName());
        this.parent = parent;
        this.draft = draft;
        this.previews = previews;
        this.ore = ore;
        this.icon = VisibleOresConfigScreen.icon(ore);
    }

    @Override
    protected void init() {
        layout = new HeaderAndFooterLayout(this);
        layout.addTitleHeader(title, font);
        list = layout.addToContents(new ConfigList(minecraft, width, layout, ROW_WIDTH));

        var current = draft.ore(ore);
        var blocks = current.apply().blocks();

        list.add(new PreviewRow());

        var dimension = CycleButton.<DimensionFilter>builder(value -> Lang.configEnum("dimension", value), current.dimension())
                .withValues(DimensionFilter.values())
                .withTooltip(value -> Tooltip.create(Lang.config(blocks ? "dimension.tooltip" : "blocks_only.tooltip")))
                .create(0, 0, 150, 20, Lang.config("dimension"), (button, value) -> update(s -> s.withDimension(value)));
        dimension.active = blocks;
        list.widgets(
                CycleButton.booleanBuilder(VisibleOresConfigScreen.onOff(true), VisibleOresConfigScreen.onOff(false), current.enabled())
                        .withTooltip(value -> Tooltip.create(Lang.config("ore.enabled.tooltip")))
                        .create(0, 0, 150, 20, Lang.config("ore.enabled"), (button, value) -> update(s -> s.withEnabled(value))),
                dimension);

        var texture = CycleButton.<TextureMode>builder(value -> Lang.configEnum("texture", value), current.texture())
                .withValues(ore.textureModes())
                .withTooltip(value -> Tooltip.create(Lang.config(ore.isOre() ? "texture.tooltip" : "texture.tooltip_basic")))
                .create(0, 0, 150, 20, Lang.config("texture"), (button, value) -> {
                    update(s -> s.withTexture(value));
                    rebuildWidgets();
                });
        var apply = CycleButton.<ApplyMode>builder(value -> Lang.configEnum("apply", value), current.apply())
                .withValues(ApplyMode.values())
                .withTooltip(value -> Tooltip.create(Lang.config("apply.tooltip")))
                .create(0, 0, 150, 20, Lang.config("apply"), (button, value) -> {
                    update(s -> s.withApply(value));
                    rebuildWidgets();
                });

        if (ore == OreType.ANCIENT_DEBRIS) {
            var custom = current.texture() == TextureMode.CUSTOM;
            var blue = CycleButton.booleanBuilder(VisibleOresConfigScreen.onOff(true), VisibleOresConfigScreen.onOff(false), current.blueDebris())
                    .withTooltip(value -> Tooltip.create(Lang.config(custom ? "blue_debris.custom_tooltip" : "blue_debris.tooltip")))
                    .create(0, 0, 150, 20, Lang.config("blue_debris"), (button, value) -> update(s -> s.withBlueDebris(value)));
            blue.active = !custom;
            list.widgets(texture, blue);
            list.widgets(apply);
        } else {
            list.widgets(texture, apply);
        }

        if (current.texture() == TextureMode.CUSTOM) {
            var pack = Button.builder(Lang.config("custom_pack.button", packName(current.customPack())),
                    button -> minecraft.gui.setScreen(new PackPickerScreen(this, draft, previews, ore))).width(ROW_WIDTH).build();
            pack.setTooltip(Tooltip.create(Lang.config("custom_pack.tooltip")));
            list.widgets(pack);
        }

        list.header(Lang.config("height_limit"));
        var height = CycleButton.booleanBuilder(VisibleOresConfigScreen.onOff(true), VisibleOresConfigScreen.onOff(false), current.heightLimit())
                .withTooltip(value -> Tooltip.create(Lang.config(blocks ? "height_limit.tooltip" : "blocks_only.tooltip")))
                .create(0, 0, ROW_WIDTH, 20, Lang.config("height_limit"), (button, value) -> {
                    update(s -> s.withHeight(value, s.minY(), s.maxY()));
                    rebuildWidgets();
                });
        height.active = blocks;
        list.widgets(height);
        if (current.heightLimit() && blocks) {
            minSlider = new YSlider(Lang.config("min_y"), current.minY(), this::setMinY);
            minSlider.setTooltip(Tooltip.create(Lang.config("min_y.tooltip")));
            maxSlider = new YSlider(Lang.config("max_y"), current.maxY(), this::setMaxY);
            maxSlider.setTooltip(Tooltip.create(Lang.config("max_y.tooltip")));
            list.widgets(minSlider, maxSlider);
        } else {
            minSlider = null;
            maxSlider = null;
        }

        var footer = layout.addToFooter(LinearLayout.horizontal().spacing(8));
        var reset = footer.addChild(Button.builder(Lang.config("screen.reset"), button -> {
            draft.setOre(ore, OreSettings.defaults(ore));
            rebuildWidgets();
        }).width(150).build());
        reset.setTooltip(Tooltip.create(Lang.config("screen.reset.tooltip")));
        footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).width(150).build());

        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
        if (list != null) list.updateSize(width, layout);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (!draft.enabled) {
            graphics.centeredText(font, Lang.config("screen.master_off"), width / 2, height - layout.getFooterHeight() - 12, 0xFFFF8A65);
        }
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    static Component packName(String name) {
        return name.isEmpty() ? Lang.config("custom_pack.none").withStyle(ChatFormatting.GRAY) : Component.literal(name);
    }

    private void setMinY(int y) {
        update(s -> s.withHeight(true, y, Math.max(y, s.maxY())));
        if (maxSlider != null && maxSlider.level() < y) maxSlider.setLevel(y);
    }

    private void setMaxY(int y) {
        update(s -> s.withHeight(true, Math.min(y, s.minY()), y));
        if (minSlider != null && minSlider.level() > y) minSlider.setLevel(y);
    }

    private void update(UnaryOperator<OreSettings> change) {
        draft.setOre(ore, change.apply(draft.ore(ore)));
    }

    private final class PreviewRow extends ConfigList.Row {
        private static final int CUBE = 44;
        private static final int SPACING = 96;

        @Override
        int height() {
            return CUBE + 24;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            var font = Minecraft.getInstance().font;
            var middle = getContentXMiddle();
            var y = getContentY() + 2;
            var labelY = y + CUBE + 5;
            var cubeWidth = BlockPreview.width(CUBE);

            var block = previews.cube(ore, draft.ore(ore), draft.enabled);
            BlockPreview.draw(graphics, previews.vanilla(ore), middle - SPACING - cubeWidth / 2F, y, CUBE);
            BlockPreview.draw(graphics, block, middle - cubeWidth / 2F, y, CUBE);
            drawItem(graphics, middle + SPACING, y + CUBE / 2);

            graphics.centeredText(font, Lang.config("preview.vanilla"), middle - SPACING, labelY, 0xFFA0A0A0);
            var blockLabel = block.approximate() ? Lang.config("preview.block").append("*") : Lang.config("preview.block");
            graphics.centeredText(font, blockLabel, middle, labelY, 0xFFFFFFFF);
            graphics.centeredText(font, Lang.config("preview.item"), middle + SPACING, labelY, 0xFFFFFFFF);

            if (block.approximate() && Math.abs(mouseX - middle) <= 40 && mouseY >= y && mouseY <= labelY + 9) {
                graphics.setTooltipForNextFrame(font, Lang.config("preview.approximate"), mouseX, mouseY);
            }
        }

        private void drawItem(GuiGraphicsExtractor graphics, int centerX, int centerY) {
            var scale = 2.5F;
            var pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(centerX - 8 * scale, centerY - 8 * scale);
            pose.scale(scale, scale);
            ConfigPreview.with(draft.toSnapshot(), () -> graphics.item(icon, 0, 0));
            pose.popMatrix();
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of();
        }
    }
}
