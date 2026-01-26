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
}
