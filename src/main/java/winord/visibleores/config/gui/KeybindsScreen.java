package winord.visibleores.config.gui;

import com.mojang.blaze3d.platform.InputConstants;
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
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import winord.visibleores.config.ConfigSnapshot;
import winord.visibleores.config.OreGroup;
import winord.visibleores.config.OreType;
import winord.visibleores.input.KeybindHandler;
import winord.visibleores.render.OrePreviews;
import winord.visibleores.util.Lang;

import java.util.ArrayList;
import java.util.List;

final class KeybindsScreen extends Screen {
    private static final int ROW_WIDTH = 310;
    private static final int KEY_WIDTH = 96;
    private static final int CLEAR_WIDTH = 20;

    private final Screen parent;
    private final ConfigDraft draft;
    private final OrePreviews previews;
    private final List<KeyRow> rows = new ArrayList<>();
    private HeaderAndFooterLayout layout;
    private ConfigList list;
    @Nullable
    private KeyRow listening;

    KeybindsScreen(Screen parent, ConfigDraft draft, OrePreviews previews) {
        super(Lang.config("category.keybinds"));
        this.parent = parent;
        this.draft = draft;
        this.previews = previews;
    }

    @Override
    protected void init() {
        rows.clear();
        listening = null;
        layout = new HeaderAndFooterLayout(this);
        layout.addTitleHeader(title, font);
        list = layout.addToContents(new ConfigList(minecraft, width, layout, ROW_WIDTH));

        list.header(Lang.config("category.general"));
        addRow(ConfigSnapshot.MASTER_KEY, Lang.config("title"), Lang.config("keybinds.master.tooltip"), null);
        for (var group : OreGroup.values()) {
            list.header(VisibleOresConfigScreen.groupTitle(group));
            for (var ore : OreType.inGroup(group)) {
                addRow(ore.key(), ore.displayName(), Lang.config("keybinds.ore.tooltip"), ore);
            }
        }

        var footer = layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(Button.builder(Lang.config("screen.reset_keys"), button -> {
            draft.clearKeybinds();
            listening = null;
            refresh();
        }).width(150).build());
        footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).width(150).build());

        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
        refresh();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
        if (list != null) list.updateSize(width, layout);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (listening != null) {
            draft.setKeybind(listening.id, event.isEscape() ? "" : KeybindHandler.format(InputConstants.getKey(event)));
            listening = null;
            refresh();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (listening != null) {
            listening = null;
            refresh();
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    private void addRow(String id, Component name, Component tooltip, @Nullable OreType ore) {
        var row = new KeyRow(id, name, tooltip, ore);
        rows.add(row);
        list.add(row);
    }

    private void refresh() {
        for (var row : rows) row.refresh();
    }

    private final class KeyRow extends ConfigList.Row {
        private final String id;
        private final Component name;
        @Nullable
        private final OreType ore;
        private final Button key;
        private final Button clear;
        private final Tooltip tooltip;

        KeyRow(String id, Component name, Component tooltip, @Nullable OreType ore) {
            this.id = id;
            this.name = name;
            this.ore = ore;
            this.key = Button.builder(Component.empty(), button -> {
                listening = this;
                KeybindsScreen.this.refresh();
            }).size(KEY_WIDTH, 20).build();
            this.tooltip = Tooltip.create(tooltip.copy().append("\n\n").append(Lang.config("keybinds.hint").withStyle(ChatFormatting.GRAY)));
            this.clear = Button.builder(Component.literal("×"), button -> {
                draft.setKeybind(id, "");
                listening = null;
                KeybindsScreen.this.refresh();
            }).size(CLEAR_WIDTH, 20).build();
            this.clear.setTooltip(Tooltip.create(Lang.config("keybinds.clear")));
        }

        void refresh() {
            var stored = draft.keybind(id);
            var label = KeybindHandler.parse(stored).getDisplayName().copy();
            key.setTooltip(tooltip);
            if (listening == this) {
                key.setMessage(Component.literal("> ").append(label.withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE)).append(" <")
                        .withStyle(ChatFormatting.YELLOW));
            } else if (draft.keyUsages(stored) > 1) {
                key.setMessage(label.withStyle(ChatFormatting.RED));
                key.setTooltip(Tooltip.create(Lang.config("keybinds.duplicate").withStyle(ChatFormatting.RED)));
            } else {
                key.setMessage(stored.isEmpty() ? label.withStyle(ChatFormatting.GRAY) : label);
            }
            clear.active = !stored.isEmpty();
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            var font = Minecraft.getInstance().font;
            var x = getContentX();
            var middle = getContentY() + getContentHeight() / 2;

            if (hovered) graphics.fill(x - 2, getContentY() - 1, getContentRight() + 2, getContentY() + getContentHeight() + 1, 0x18FFFFFF);
            var textX = x + 2;
            if (ore != null) {
                BlockPreview.draw(graphics, previews.vanilla(ore), x + 2 + (16 - BlockPreview.width(18)) / 2F, middle - 9, 18);
                textX = x + 24;
            }

            clear.setPosition(getContentRight() - CLEAR_WIDTH, middle - 10);
            key.setPosition(clear.getX() - 4 - KEY_WIDTH, middle - 10);
            var textWidth = key.getX() - 6 - textX;
            graphics.text(font, VisibleOresConfigScreen.fit(font, name.getString(), textWidth), textX, middle - 4, 0xFFFFFFFF);

            key.extractRenderState(graphics, mouseX, mouseY, partialTick);
            clear.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(key, clear);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(key, clear);
        }
    }
}
