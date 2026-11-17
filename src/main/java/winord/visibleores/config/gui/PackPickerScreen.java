package winord.visibleores.config.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import winord.visibleores.config.CustomPacks;
import winord.visibleores.config.OreType;
import winord.visibleores.render.OrePreviews;
import winord.visibleores.util.Lang;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class PackPickerScreen extends Screen {
    private static final int ROW_WIDTH = 310;
    private static final int ROW_HEIGHT = 40;
    private static final int ICON = 32;
    private static final int CUBE = 30;
    private static final Identifier UNKNOWN_PACK = Identifier.withDefaultNamespace("textures/misc/unknown_pack.png");

    private final Screen parent;
    private final ConfigDraft draft;
    private final OrePreviews previews;
    private final OreType ore;
    private HeaderAndFooterLayout layout;
    private ConfigList list;

    PackPickerScreen(Screen parent, ConfigDraft draft, OrePreviews previews, OreType ore) {
        super(Lang.config("picker.title", ore.displayName()));
        this.parent = parent;
        this.draft = draft;
        this.previews = previews;
        this.ore = ore;
        Minecraft.getInstance().getResourcePackRepository().reload();
    }

    @Override
    protected void init() {
        layout = new HeaderAndFooterLayout(this);
        layout.addTitleHeader(title, font);
        list = layout.addToContents(new ConfigList(minecraft, width, layout, ROW_WIDTH));

        var names = new ArrayList<>(CustomPacks.availableNames());
        names.sort(Comparator.comparing((String name) -> !previews.changes(ore, name)).thenComparing(String.CASE_INSENSITIVE_ORDER));
        list.add(new PackRow(""));
        for (var name : names) list.add(new PackRow(name));
        if (names.isEmpty()) list.header(Lang.config("picker.empty"));

        var footer = layout.addToFooter(LinearLayout.horizontal().spacing(8));
        var folder = footer.addChild(Button.builder(Lang.config("picker.open_folder"),
                button -> defaultHandleClickEvent(new ClickEvent.OpenFile(minecraft.getResourcePackDirectory()), minecraft, this)).width(150).build());
        folder.setTooltip(Tooltip.create(Lang.config("picker.open_folder.tooltip")));
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
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    private void select(String pack) {
        draft.setOre(ore, draft.ore(ore).withPack(pack));
        onClose();
    }

    private final class PackRow extends ConfigList.Row {
        private final String pack;
        private final Component title;
        private final Component description;

        PackRow(String pack) {
            this.pack = pack;
            if (pack.isEmpty()) {
                title = Lang.config("custom_pack.none");
                description = Lang.config("picker.none.description");
            } else {
                var info = Minecraft.getInstance().getResourcePackRepository().getPack(CustomPacks.packId(pack));
                title = info != null ? info.getTitle() : Component.literal(pack);
                description = info != null ? info.getDescription() : Component.empty();
            }
        }

        @Override
        int height() {
            return ROW_HEIGHT;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            var font = Minecraft.getInstance().font;
            var x = getContentX();
            var y = getContentY();
            var right = getContentRight();
            var bottom = y + getContentHeight();
            var selected = pack.equals(draft.ore(ore).customPack());
            var changes = pack.isEmpty() || previews.changes(ore, pack);

            if (selected) {
                graphics.fill(x - 2, y - 2, right + 2, bottom + 2, 0x30FFFFFF);
                graphics.outline(x - 2, y - 2, right - x + 4, bottom - y + 4, 0xFFFFFFFF);
            } else if (hovered) {
                graphics.fill(x - 2, y - 2, right + 2, bottom + 2, 0x18FFFFFF);
            }

            var iconY = y + (getContentHeight() - ICON) / 2;
            var icon = pack.isEmpty() ? null : previews.packIcon(pack);
            if (icon != null) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, icon.texture(), x, iconY, 0F, 0F, ICON, ICON, icon.width(), icon.height(), icon.width(), icon.height());
            } else if (!pack.isEmpty()) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, UNKNOWN_PACK, x, iconY, 0F, 0F, ICON, ICON, ICON, ICON);
            }

            var cubeWidth = BlockPreview.width(CUBE);
            BlockPreview.draw(graphics, previews.pack(ore, pack), right - cubeWidth - 4, y + (getContentHeight() - CUBE) / 2F, CUBE);

            var textX = pack.isEmpty() ? x + 4 : x + ICON + 6;
            var textWidth = right - cubeWidth - 12 - textX;
            graphics.text(font, VisibleOresConfigScreen.fit(font, title.getString(), textWidth), textX, y + 3, changes ? 0xFFFFFFFF : 0xFF9A9A9A);
            var lines = font.split(description, textWidth);
            if (!lines.isEmpty()) graphics.text(font, lines.getFirst(), textX, y + 14, 0xFF9A9A9A);
            if (!pack.isEmpty()) {
                var status = changes
                        ? Lang.config("picker.changes").withStyle(ChatFormatting.GREEN)
                        : Lang.config("picker.no_changes").withStyle(ChatFormatting.DARK_GRAY);
                graphics.text(font, status, textX, y + 25, 0xFFFFFFFF);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            select(pack);
            return true;
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            if (event.isConfirmation()) {
                select(pack);
                return true;
            }
            return super.keyPressed(event);
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
