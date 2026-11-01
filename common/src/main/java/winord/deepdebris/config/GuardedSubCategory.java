package winord.deepdebris.config;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

import java.util.List;

final class GuardedSubCategory extends SubCategoryListEntry {
    @SuppressWarnings("deprecation")
    GuardedSubCategory(Component name, List<AbstractConfigListEntry> entries) {
        super(name, entries, false);
    }

    @Override
    public void setFocused(GuiEventListener focused) {
        if (getFocused() != focused) super.setFocused(focused);
    }
}
