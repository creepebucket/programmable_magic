package org.creepebucket.programmable_magic.gui.lib.api;

import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * 键值数据管理器：为 UI 提供本地存储与 c2s/s2c 的 KV 同步。
 */
public class DataManager {

    /**
     * 拉取模式使用的内部键：客户端上报需要拉取的 keys，服务端返回这些 keys 的当前值。
     */
    public static final String KEY_PULL = "data_manager.pull_keys";

    private static final BiConsumer<String, Object> NOOP = (k, v) -> {};

    private record Meta(DataType type, SyncMode syncMode) {}

    private final Map<String, Meta> metas = new HashMap<>();
    private final Map<String, Object> values = new HashMap<>();
    private final Set<String> pendingPullKeys = new LinkedHashSet<>();

    private BiConsumer<String, Object> sendToServer = NOOP;
    private BiConsumer<String, Object> sendToClient = NOOP;

    /**
     * 绑定客户端 -> 服务端发送器。
     */
    public void bindSendToServer(BiConsumer<String, Object> sender) { this.sendToServer = sender; }

    /**
     * 绑定服务端 -> 客户端发送器。
     */
    public void bindSendToClient(BiConsumer<String, Object> sender) { this.sendToClient = sender; }

    /**
     * 注册一个键并返回实例句柄；若已存在则复用既有元信息与初始值。
     */
    public DataInstance request(String key, DataType type, SyncMode syncMode, Object initialValue) {
        if (initialValue == null) throw new IllegalArgumentException("initialValue is required: " + key);
        this.metas.putIfAbsent(key, new Meta(type, syncMode));
        this.values.putIfAbsent(key, initialValue);

        if ((syncMode == SyncMode.S2C || syncMode == SyncMode.BOTH) && this.sendToClient == NOOP) this.pendingPullKeys.add(key);
        return new DataInstance(this, key, type, syncMode);
    }

    /**
     * 判断某个键是否已注册。
     */
    public boolean hasKey(String key) { return this.metas.containsKey(key); }

    /**
     * 读取键的当前值。
     */
    public Object get(String key) { return this.values.get(key); }

    /**
     * 仅写入本地值，不触发同步发送。
     */
    public void setLocal(String key, Object value) {
        metaOrThrow(key);
        this.values.put(key, value);
    }

    /**
     * 写入值，并按键的同步模式决定是否发送到对端。
     */
    public void set(String key, Object value) {
        Meta meta = metaOrThrow(key);
        if (this.sendToServer != NOOP && meta.syncMode() == SyncMode.S2C) throw new IllegalStateException("client cannot set s2c key: " + key);
        if (this.sendToClient != NOOP && meta.syncMode() == SyncMode.C2S) throw new IllegalStateException("server cannot set c2s key: " + key);

        this.values.put(key, value);

        if ((meta.syncMode() == SyncMode.C2S || meta.syncMode() == SyncMode.BOTH) && this.sendToServer != NOOP) this.sendToServer.accept(key, value);
        if ((meta.syncMode() == SyncMode.S2C || meta.syncMode() == SyncMode.BOTH) && this.sendToClient != NOOP) this.sendToClient.accept(key, value);
    }

    /**
     * 处理客户端 -> 服务端的数据包；返回 {@code true} 表示该键被本管理器消费。
     */
    public boolean handleC2S(String key, Object value) {
        if (KEY_PULL.equals(key)) {
            for (String requestedKey : ((String) value).split("\n")) {
                metaOrThrow(requestedKey);
                Object requestedValue = get(requestedKey);
                if (requestedValue == null) throw new IllegalStateException("null value: " + requestedKey);
                this.sendToClient.accept(requestedKey, requestedValue);
            }
            return true;
        }
        if (!this.metas.containsKey(key)) return false;
        setLocal(key, value);
        this.sendToClient.accept(key, get(key));
        return true;
    }

    /**
     * 处理服务端 -> 客户端的数据包；返回 {@code true} 表示该键被本管理器消费。
     */
    public boolean handleS2C(String key, Object value) {
        if (!this.metas.containsKey(key)) return false;
        setLocal(key, value);
        return true;
    }

    /**
     * 将暂存的拉取 keys 作为一个 KEY_PULL 包发送给服务端。
     */
    public void flushPullRequests() {
        if (this.pendingPullKeys.isEmpty()) return;
        if (this.sendToServer == NOOP) return;
        this.sendToServer.accept(KEY_PULL, String.join("\n", this.pendingPullKeys));
        this.pendingPullKeys.clear();
    }

    /**
     * 获取键的元信息；若未注册则抛出异常。
     */
    private Meta metaOrThrow(String key) {
        Meta meta = this.metas.get(key);
        if (meta == null) throw new IllegalStateException("unknown key: " + key);
        return meta;
    }
}
