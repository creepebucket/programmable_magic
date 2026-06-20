package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.gui.lib.api.Color;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.DynamicValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Lifecycle;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Tickable;

import java.util.Objects;

import static java.lang.Double.parseDouble;

public class NumberInputWidget extends Widget implements Lifecycle, Tickable {
    public DynamicValue<Double> num;
    public double min, max, step = 1, fact = 10;
    public int depth = 2;
    public boolean showMinMax = true, buttonPressed = false;
    public InputBoxWidget inputBox;

    public NumberInputWidget(Coordinate pos, Coordinate size, DynamicValue<Double> num, double min, double max) {
        super(pos, size);

        this.num = num;
        this.min = min;
        this.max = max;
    }

    @Override
    public void onInitialize() {
        // 最值按钮
        if (showMinMax) {
            addChild(new TextButtonWidget(Coordinate.fromTopLeft(0, 0), Coordinate.fromTopLeft(h(), h()), Component.literal("|<"), () -> {num.set(min); buttonPressed = true;}));
            addChild(new TextButtonWidget(Coordinate.fromTopRight(0, 0), Coordinate.fromTopLeft(h(), h()), Component.literal(">|"), () -> {num.set(max); buttonPressed = true;}).rightAlign());
        }

        // 普通按钮
        for (int i = 0; i < depth; i++) {
            int finalI = i;
            addChild(new TextButtonWidget(Coordinate.fromTopLeft((h() + 1) * i + (showMinMax ? h() + 1 : 0), 0), Coordinate.fromTopLeft(h(), h()),
                    Component.literal("<".repeat(depth - i)), () -> {
                num.set(Math.max(min, num.get() - step * Math.pow(fact, depth - finalI - 1)));
                buttonPressed = true;
            }));
            addChild(new TextButtonWidget(Coordinate.fromTopLeft(w() - (h() + 1) * i - (showMinMax ? h() + 1 : 0), 0), Coordinate.fromTopLeft(h(), h()),
                    Component.literal(">".repeat(depth - i)), () -> {
                num.set(Math.min(max, num.get() + step * Math.pow(fact, depth - finalI - 1)));
                buttonPressed = true;
            }).rightAlign());
        }

        // 输入框
        inputBox = (InputBoxWidget) addChild(new InputBoxWidget(Coordinate.fromTopLeft((h() + 1) * depth + (showMinMax ? h() + 1 : 0), 0), Coordinate.fromTopLeft(w() - 2 * ((h() + 1) * depth + (showMinMax ? h() + 1 : 0)), h()),
                String.valueOf(num.get()), 1024).mainColor(new Color(0)));
    }

    @Override
    public void tick() {
        if (!buttonPressed && !Objects.equals(String.valueOf(num.get()), inputBox.box.getValue())) {
            try {
                var d = parseDouble(inputBox.box.getValue());
                num.set(Math.clamp(d, min, max));
                if (d != num.get()) {
                    inputBox.box.setValue(String.valueOf(num.get()));
                }
                inputBox.bgColor(bgColor()).tooltip(null);
            } catch (NumberFormatException e) {
                if (!inputBox.box.getValue().isEmpty() && !inputBox.box.getValue().equals("-")) {
                    inputBox.box.setValue(String.valueOf(num.get()));
                }
            }
        } else if (buttonPressed) {
            inputBox.box.setValue(String.valueOf(num.get()));
        }

        buttonPressed = false;
    }

    public NumberInputWidget setDepth(int depth) {
        this.depth = depth;
        return this;
    }

    public NumberInputWidget setStep(double step) {
        this.step = step;
        return this;
    }

    public NumberInputWidget setFactor(double fact) {
        this.fact = fact;
        return this;
    }

    public NumberInputWidget disableMinMaxButton() {
        showMinMax = false;
        return this;
    }

    public NumberInputWidget enableMinMaxButton() {
        showMinMax = true;
        return this;
    }

    public void update() {
        children.clear();
        onInitialize();
    }
}
