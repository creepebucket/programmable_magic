package org.creepebucket.programmable_magic.gui.lib.api;

import java.util.function.Supplier;

public class DynamicValue<T> {
    private final DataManager manager;
    private final String key;

    DynamicValue(DataManager manager, String key) {
        this.manager = manager;
        this.key = key;
    }

    @SuppressWarnings("unchecked")
    public T get() {
        return (T) manager.values.get(key);
    }

    public void set(T value) {
        manager.update(key, value);
    }

    public DynamicValue<T> whenFirstDataArrivesDo(Runnable hook) {
        if (manager != null) manager.onFirstArrival.put(key, hook);
        return this;
    }

    /**
     * 从函数里拆, 适用于DynamicValue<Map<?, ?>> 拆键的情况
     */
    public static <T> DynamicValue<T> fromSupplier(Supplier<T> supplier) {
        return new DynamicValue<T>(null, null) {
            @Override
            public T get() {
                return supplier.get();
            }

            @Override
            public void set(T value) {
            }
        };
    }

    public static <T> DynamicValue<T> staticValue(T value) {
        return new StaticValue<>(value);
    }

    /**
     * 固定值, 适用于一些特殊情况
     */
    public static class StaticValue<T> extends DynamicValue<T> {
        T value;

        public StaticValue(T value) {
            super(null, null);
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public void set(T value) {
            // 不允许写入
        }
    }
}
