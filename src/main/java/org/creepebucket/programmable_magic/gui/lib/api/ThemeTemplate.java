package org.creepebucket.programmable_magic.gui.lib.api;

import java.util.function.Function;

public class ThemeTemplate<T extends Widget> {
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
