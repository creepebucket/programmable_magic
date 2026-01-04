package org.creepebucket.programmable_magic.gui.lib.api;

import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class DataManager {

    public static final String KEY_PULL = "data_manager.pull_keys";

    private static final BiConsumer<String, Object> NOOP = (k, v) -> {};

    private record Meta(DataType type, SyncMode syncMode) {}

    private final Map<String, Meta> metas = new HashMap<>();
    private final Map<String, Object> values = new HashMap<>();
    private final Set<String> pendingPullKeys = new LinkedHashSet<>();

    private BiConsumer<String, Object> sendToServer = NOOP;
    private BiConsumer<String, Object> sendToClient = NOOP;

    public void bindSendToServer(BiConsumer<String, Object> sender) { this.sendToServer = sender; }
    public void bindSendToClient(BiConsumer<String, Object> sender) { this.sendToClient = sender; }

    public DataInstance request(String key, DataType type, SyncMode syncMode, Object initialValue) {
        if (initialValue == null) throw new IllegalArgumentException("initialValue is required: " + key);
        this.metas.putIfAbsent(key, new Meta(type, syncMode));
        this.values.putIfAbsent(key, initialValue);

        if ((syncMode == SyncMode.S2C || syncMode == SyncMode.BOTH) && this.sendToClient == NOOP) this.pendingPullKeys.add(key);
        return new DataInstance(this, key, type, syncMode);
    }

    public boolean hasKey(String key) { return this.metas.containsKey(key); }

    public Object get(String key) { return this.values.get(key); }

    public void setLocal(String key, Object value) {
        Meta meta = this.metas.get(key);
        if (meta == null) throw new IllegalStateException("unknown key: " + key);
        assertType(meta.type(), value);
        this.values.put(key, value);
    }

    public void set(String key, Object value) {
        Meta meta = this.metas.get(key);
        if (meta == null) throw new IllegalStateException("unknown key: " + key);
        assertType(meta.type(), value);

        boolean isClient = this.sendToServer != NOOP;
        boolean isServer = this.sendToClient != NOOP;
        if (isClient && meta.syncMode() == SyncMode.S2C) throw new IllegalStateException("client cannot set s2c key: " + key);
        if (isServer && meta.syncMode() == SyncMode.C2S) throw new IllegalStateException("server cannot set c2s key: " + key);

        this.values.put(key, value);

        if ((meta.syncMode() == SyncMode.C2S || meta.syncMode() == SyncMode.BOTH) && this.sendToServer != NOOP) this.sendToServer.accept(key, value);
        if ((meta.syncMode() == SyncMode.S2C || meta.syncMode() == SyncMode.BOTH) && this.sendToClient != NOOP) this.sendToClient.accept(key, value);
    }

    public boolean handleC2S(String key, Object value) {
        if (KEY_PULL.equals(key)) {
            String joined = (String) value;
            for (String requestedKey : joined.split("\n")) {
                Meta meta = this.metas.get(requestedKey);
                if (meta == null) throw new IllegalStateException("unknown key: " + requestedKey);
                Object v = get(requestedKey);
                if (v == null) throw new IllegalStateException("null value: " + requestedKey);
                this.sendToClient.accept(requestedKey, v);
            }
            return true;
        }
        if (!this.metas.containsKey(key)) return false;
        setLocal(key, value);
        this.sendToClient.accept(key, get(key));
        return true;
    }

    public boolean handleS2C(String key, Object value) {
        if (!this.metas.containsKey(key)) return false;
        setLocal(key, value);
        return true;
    }

    public void flushPullRequests() {
        if (this.pendingPullKeys.isEmpty()) return;
        if (this.sendToServer == NOOP) return;
        this.sendToServer.accept(KEY_PULL, String.join("\n", this.pendingPullKeys));
        this.pendingPullKeys.clear();
    }

    private void assertType(DataType type, Object value) {
        boolean ok = switch (type) {
            case STRING -> value instanceof String;
            case INT -> value instanceof Integer;
            case DOUBLE -> value instanceof Double;
            case BOOLEAN -> value instanceof Boolean;
        };
        if (!ok) throw new IllegalArgumentException("type mismatch: " + type + " value=" + value);
    }
}
