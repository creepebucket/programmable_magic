package org.creepebucket.arcanism.gui.lib.api;

import org.creepebucket.arcanism.gui.lib.widgets.RectangleWidget;
import org.creepebucket.arcanism.gui.lib.widgets.TextWidget;

import java.util.function.Function;

public class ThemeTemplate<T extends Widget> {
	public static final ThemeTemplate<RectangleWidget> DARK_BG = new ThemeTemplate<>(w -> { w.mainColor(0x7f000000); return w; });
	public static final ThemeTemplate<RectangleWidget> TITLE_BAR_BG = new ThemeTemplate<>(w -> { w.mainColor(0xbf000000); return w; });

	public static final ThemeTemplate<TextWidget> NO_SHADOW = new ThemeTemplate<>(TextWidget::noShadow);
	public static final ThemeTemplate<TextWidget> WHITE = new ThemeTemplate<>(w -> { w.mainColor(-1); return w; });
	public static final ThemeTemplate<TextWidget> GRAY = new ThemeTemplate<>(w -> { w.mainColor(0xff7f7f7f); return w; });
	public static final ThemeTemplate<TextWidget> BRIGHT_GRAY = new ThemeTemplate<>(w -> { w.mainColor(0xffbfbfbf); return w; });
	public static final ThemeTemplate<TextWidget> HALF_WHITE = new ThemeTemplate<>(w -> { w.mainColor(0x7fffffff); return w; });
	public static final ThemeTemplate<TextWidget> HALF_GRAY = new ThemeTemplate<>(w -> { w.mainColor(0x7f7f7f7f); return w; });
	public static final ThemeTemplate<TextWidget> HALF_SCALE = new ThemeTemplate<>(w -> { w.scaled(0.5); return w; });

	public static final ThemeTemplate<TextWidget> GENERAL_TEXT = NO_SHADOW.then(WHITE);
	public static final ThemeTemplate<TextWidget> LABEL_TEXT = NO_SHADOW.then(GRAY);
	public static final ThemeTemplate<TextWidget> BRIGHT_LABEL = NO_SHADOW.then(BRIGHT_GRAY);
	public static final ThemeTemplate<TextWidget> DIM_TEXT = NO_SHADOW.then(HALF_WHITE);
	public static final ThemeTemplate<TextWidget> HALF_LABEL = HALF_SCALE.then(NO_SHADOW).then(HALF_GRAY);

	public Function<T, T> function;

	public ThemeTemplate(Function<T, T> function) {
		this.function = function;
	}

	public T apply(T widget) {
		return function.apply(widget);
	}

	public ThemeTemplate<T> then(ThemeTemplate<T> other) {
		return new ThemeTemplate<>(w -> other.function.apply(function.apply(w)));
	}
}
