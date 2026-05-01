package org.creepebucket.programmable_magic.gui.lib.api;

public class SyncedValue<T> {
    private final DataManager manager;
    private final String key;

    SyncedValue(DataManager manager, String key) {
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

    /**
     * 固定值, 适用于一些特殊情况
     */
    public static class StaticDouble extends SyncedValue<Double> {
        Double n;

        public StaticDouble(Double n) {
            super(null, null);
            this.n = n;
        }

        @Override
        public Double get() {
            return n;
        }

        @Override
        public void set(Double value) {
            // 不允许写入
        }
    }
}
