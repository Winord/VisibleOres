package winord.visibleores.config.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import winord.visibleores.config.ApplyMode;
import winord.visibleores.config.ConfigPreview;
import winord.visibleores.config.OreGroup;
import winord.visibleores.config.OreSettings;
import winord.visibleores.config.OreType;
import winord.visibleores.config.TextureMode;
import winord.visibleores.config.VisibleOresConfig;
import winord.visibleores.render.OrePreviews;
import winord.visibleores.util.Lang;

import java.util.List;

public final class VisibleOresConfigScreen extends Screen {
    private static final int ROW_WIDTH = 310;
    private static final int ORE_ROW_HEIGHT = 28;
    private static final int TOGGLE_WIDTH = 44;
    private static final int EDIT_WIDTH = 82;

    private final Screen parent;
    private final ConfigDraft draft;
    private final OrePreviews previews = new OrePreviews();
    private HeaderAndFooterLayout layout;
    private ConfigList list;
    private double scroll;

    public VisibleOresConfigScreen(Screen parent) {
        super(Lang.config("title"));
        this.parent = parent;
        this.draft = new ConfigDraft(VisibleOresConfig.get());
    }

    @Override
    protected void init() {
        layout = new HeaderAndFooterLayout(this);
        layout.addTitleHeader(title, font);

        list = layout.addToContents(new ConfigList(minecraft, width, layout, ROW_WIDTH));
        list.header(Lang.config("category.general"));
        list.widgets(CycleButton.booleanBuilder(onOff(true), onOff(false), draft.enabled)
                .withTooltip(value -> Tooltip.create(Lang.config("enabled.tooltip")))
                .create(0, 0, ROW_WIDTH, 20, Lang.config("title"), (button, value) -> draft.enabled = value));

        for (var group : OreGroup.values()) {
            list.header(groupTitle(group));
            addOres(group);
        }

        var footer = layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(Button.builder(Lang.config("screen.keybinds"), button -> open(new KeybindsScreen(this, draft, previews))).width(100).build());
        footer.addChild(Button.builder(Lang.config("screen.reset_all"), button -> confirmReset()).width(100).build());
        footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).width(100).build());

        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
        list.setScrollAmount(scroll);
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
        if (list != null) list.updateSize(width, layout);
    }

    @Override
    public void onClose() {
        VisibleOresConfig.apply(draft.toSnapshot());
        minecraft.gui.setScreen(parent);
        previews.close();
    }

    private void addOres(OreGroup group) {
        for (var ore : OreType.inGroup(group)) list.add(new OreRow(ore));
    }

    private void open(Screen screen) {
        scroll = list.scrollAmount();
        minecraft.gui.setScreen(screen);
    }

    private void confirmReset() {
        open(new ConfirmScreen(confirmed -> {
            if (confirmed) draft.resetOres();
            minecraft.gui.setScreen(this);
        }, Lang.config("screen.reset_all.title"), Lang.config("screen.reset_all.message")));
    }

    static Component groupTitle(OreGroup group) {
        return switch (group) {
            case NETHER -> Lang.config("category.nether");
            case STONE, DEEPSLATE -> Lang.config("category.overworld").append(" · ").append(Lang.configEnum("group", group));
            case OTHER -> Lang.config("category.other");
        };
    }

    static Component onOff(boolean value) {
        return Lang.message(value ? "on" : "off").withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    @Nullable
    static ItemStack icon(OreType ore) {
        var item = ore.iconBlock().asItem();
        return item.builtInRegistryHolder().areComponentsBound() ? new ItemStack(item) : null;
    }

    static String fit(Font font, String text, int width) {
        if (font.width(text) <= width) return text;
        var ellipsis = "…";
        var end = text.length();
        while (end > 0 && font.width(text.substring(0, end) + ellipsis) > width) end--;
        return text.substring(0, end) + ellipsis;
    }

    static Component summary(OreSettings settings) {
        if (!settings.enabled()) return Lang.config("summary.off");
        var text = Component.empty();
        if (settings.texture() == TextureMode.CUSTOM && !settings.customPack().isEmpty()) {
            text.append(settings.customPack());
        } else {
            text.append(Lang.configEnum("texture", settings.texture()));
        }
        if (settings.blueDebris() && settings.texture() != TextureMode.CUSTOM) text.append(" · ").append(Lang.config("summary.blue"));
        if (settings.apply() != ApplyMode.BLOCK) text.append(" · ").append(Lang.configEnum("apply", settings.apply()));
        if (settings.apply().blocks()) {
            text.append(" · ").append(Lang.configEnum("dimension", settings.dimension()));
            if (settings.heightLimit()) text.append(" · ").append(Lang.config("summary.height", settings.minY(), settings.maxY()));
        }
        return text;
    }

    private final class OreRow extends ConfigList.Row {
        private final OreType ore;
        @Nullable
        private final ItemStack icon;
        private final CycleButton<Boolean> toggle;
        private final Button edit;

        OreRow(OreType ore) {
            this.ore = ore;
            this.icon = icon(ore);
            this.toggle = CycleButton.booleanBuilder(onOff(true), onOff(false), draft.ore(ore).enabled())
                    .displayOnlyValue()
                    .withTooltip(value -> Tooltip.create(Lang.config("ore.enabled.tooltip")))
                    .create(0, 0, TOGGLE_WIDTH, 20, ore.displayName(),
                            (button, value) -> draft.setOre(ore, draft.ore(ore).withEnabled(value)));
            this.edit = Button.builder(Lang.config("screen.edit"), button -> open(new OreSettingsScreen(VisibleOresConfigScreen.this, draft, previews, ore)))
                    .size(EDIT_WIDTH, 20)
                    .build();
            this.edit.setTooltip(Tooltip.create(Lang.config("screen.edit.tooltip")));
        }

        @Override
        int height() {
            return ORE_ROW_HEIGHT;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            var font = Minecraft.getInstance().font;
            var x = getContentX();
            var y = getContentY();
            var middle = y + getContentHeight() / 2;

            if (hovered) graphics.fill(x - 2, y - 1, getContentRight() + 2, y + getContentHeight() + 1, 0x18FFFFFF);
            var settings = draft.ore(ore);
            if (!settings.apply().blocks() && icon != null) {
                ConfigPreview.with(draft.toSnapshot(), () -> graphics.item(icon, x + 2, middle - 8));
            } else {
                var shown = settings.apply().blocks() ? settings : settings.withApply(ApplyMode.BOTH);
                BlockPreview.draw(graphics, previews.cube(ore, shown, draft.enabled), x + 2 + (16 - BlockPreview.width(18)) / 2F, middle - 9, 18);
            }

            edit.setPosition(getContentRight() - EDIT_WIDTH, middle - 10);
            toggle.setPosition(edit.getX() - 4 - TOGGLE_WIDTH, middle - 10);

            var textX = x + 24;
            var textWidth = toggle.getX() - 6 - textX;
            var active = draft.enabled;
            graphics.text(font, fit(font, ore.displayName().getString(), textWidth), textX, middle - 9, active ? 0xFFFFFFFF : 0xFF8A8A8A);
            graphics.text(font, fit(font, summary(draft.ore(ore)).getString(), textWidth), textX, middle + 1, active ? 0xFFA8A8A8 : 0xFF6A6A6A);

            toggle.extractRenderState(graphics, mouseX, mouseY, partialTick);
            edit.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(toggle, edit);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(toggle, edit);
        }
    }
}
