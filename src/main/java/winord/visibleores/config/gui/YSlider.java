package winord.visibleores.config.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import winord.visibleores.config.OreSettings;

import java.util.function.IntConsumer;

/** Slider over the world height range; arrow keys move it by exactly one block. */
final class YSlider extends AbstractSliderButton {
    private static final int RANGE = OreSettings.MAX_Y - OreSettings.MIN_Y;

    private final Component label;
    private final IntConsumer onChange;
    private int level;

    YSlider(Component label, int level, IntConsumer onChange) {
        super(0, 0, 150, 20, Component.empty(), toValue(level));
        this.label = label;
        this.onChange = onChange;
        this.level = level;
        updateMessage();
    }

    int level() {
        return level;
    }

    /** Moves the slider without notifying the listener; used to keep the other bound in order. */
    void setLevel(int level) {
        this.level = Mth.clamp(level, OreSettings.MIN_Y, OreSettings.MAX_Y);
        this.value = toValue(this.level);
        updateMessage();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isLeft() || event.isRight()) {
            var next = Mth.clamp(level + (event.isLeft() ? -1 : 1), OreSettings.MIN_Y, OreSettings.MAX_Y);
            if (next != level) {
                setLevel(next);
                onChange.accept(level);
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    protected void updateMessage() {
        setMessage(CommonComponents.optionNameValue(label, Component.literal(Integer.toString(level))));
    }

    @Override
    protected void applyValue() {
        var next = OreSettings.MIN_Y + (int) Math.round(value * RANGE);
        if (next == level) return;
        level = next;
        onChange.accept(level);
    }

    private static double toValue(int level) {
        return (double) (Mth.clamp(level, OreSettings.MIN_Y, OreSettings.MAX_Y) - OreSettings.MIN_Y) / RANGE;
    }
}
