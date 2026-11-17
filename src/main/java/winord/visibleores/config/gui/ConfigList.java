package winord.visibleores.config.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Scrollable list of rows shared by all Visible Ores config screens. */
final class ConfigList extends ContainerObjectSelectionList<ConfigList.Row> {
    static final int ROW_HEIGHT = 24;
    static final int HEADER_HEIGHT = 20;
    static final int GAP = 8;

    private final int rowWidth;

    ConfigList(Minecraft minecraft, int width, HeaderAndFooterLayout layout, int rowWidth) {
        super(minecraft, width, layout.getContentHeight(), layout.getHeaderHeight(), ROW_HEIGHT);
        this.rowWidth = rowWidth;
    }

    void add(Row row) {
        addEntry(row, row.height());
    }

    void header(Component text) {
        add(new Header(text));
    }

    void widgets(AbstractWidget... widgets) {
        add(new Widgets(List.of(widgets)));
    }

    @Override
    public int getRowWidth() {
        return rowWidth;
    }

    abstract static class Row extends ContainerObjectSelectionList.Entry<Row> {
        int height() {
            return ROW_HEIGHT;
        }
    }

    /** Centered section title. */
    static final class Header extends Row {
        private final Component text;

        Header(Component text) {
            this.text = text;
        }

        @Override
        int height() {
            return HEADER_HEIGHT;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            var font = Minecraft.getInstance().font;
            var y = getContentY() + (getContentHeight() - font.lineHeight) / 2 + 1;
            var textWidth = font.width(text);
            var middle = getContentXMiddle();
            var lineY = y + font.lineHeight / 2 - 1;
            graphics.fill(getContentX(), lineY, middle - textWidth / 2 - 6, lineY + 1, 0x40FFFFFF);
            graphics.fill(middle + textWidth / 2 + 6, lineY, getContentRight(), lineY + 1, 0x40FFFFFF);
            graphics.centeredText(font, text, middle, y, 0xFFFFD866);
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

    /** One to three widgets sharing the row width equally. */
    static final class Widgets extends Row {
        private final List<AbstractWidget> widgets;

        Widgets(List<AbstractWidget> widgets) {
            this.widgets = widgets;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            var count = widgets.size();
            var width = (getContentWidth() - GAP * (count - 1)) / count;
            for (var i = 0; i < count; i++) {
                var widget = widgets.get(i);
                widget.setX(getContentX() + i * (width + GAP));
                widget.setY(getContentY());
                widget.setWidth(width);
                widget.extractRenderState(graphics, mouseX, mouseY, partialTick);
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return widgets;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return widgets;
        }
    }
}
