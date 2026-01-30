package org.creepebucket.programmable_magic.gui.lib.api;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class DataManager {

    // 内部协议常量
    private static final String KEY_PULL_REQUEST = "$internal:pull";

    // 核心存储
    final Map<String, Object> values = new HashMap<>();
    final Map<String, SyncMode> modes = new HashMap<>();

    // 待拉取列表 (客户端用)
    private final Set<String> pendingPulls = new LinkedHashSet<>();

    // 网络回调
    private BiConsumer<String, Object> sendToServer;
    private BiConsumer<String, Object> sendToClient;

    // 绑定网络发送器
    public void bindServerSender(BiConsumer<String, Object> sender) {
        this.sendToServer = sender;
    }

    public void bindClientSender(BiConsumer<String, Object> sender) {
        this.sendToClient = sender;
    }

    /**
     * 1. 注册数据
     * 如果是 S2C 且当前没有发往客户端的能力(说明是客户端)，自动加入拉取列表。
     */
    public <T> SyncedValue<T> register(String key, SyncMode mode, T initialValue) {
        if (!values.containsKey(key)) {

            values.put(key, initialValue);
            modes.put(key, mode);
        }

        // 客户端侧逻辑：如果是服务端控制的数据，启动时需要去拉取
        if ((mode == SyncMode.S2C || mode == SyncMode.BOTH) && sendToClient == null) {
            pendingPulls.add(key);
        }
        return new SyncedValue<>(this, key);
    }

    /**
     * 2. 发送拉取请求 (客户端调用)
     * 把所有积压的 key 打包发给服务端
     */
    public void flushPullRequests() {
        if (!pendingPulls.isEmpty() && sendToServer != null) {
            sendToServer.accept(KEY_PULL_REQUEST, String.join("\n", pendingPulls));
            pendingPulls.clear();
        }
    }

    /**
     * 3. 核心写逻辑 (由 SyncedValue 调用)
     * 更新本地 -> 判断模式 -> 发送网络包
     */
    void update(String key, Object newVal) {
        // 更新本地
        values.put(key, newVal);

        // 尝试发送网络包
        SyncMode mode = modes.get(key);

        // 如果我是客户端 (有发往服务端的通道) 且模式允许 C2S
        if (sendToServer != null && (mode == SyncMode.C2S || mode == SyncMode.BOTH)) {
            sendToServer.accept(key, newVal);
        }

        // 如果我是服务端 (有发往客户端的通道) 且模式允许 S2C
        if (sendToClient != null && (mode == SyncMode.S2C || mode == SyncMode.BOTH)) {
            sendToClient.accept(key, newVal);
        }
    }

    /**
     * 4. 核心读逻辑 (网络包入口)
     * 处理拉取请求 OR 处理数据更新
     */
    public boolean handlePacket(String key, Object val) {
        // === 情况 A: 处理拉取请求 (服务端逻辑) ===
        if (KEY_PULL_REQUEST.equals(key)) {
            if (sendToClient == null) return true; // 没法回话就忽略
            // 解析 key 列表，直接把当前值发回去
            for (String reqKey : ((String) val).split("\n")) {
                if (values.containsKey(reqKey)) {
                    sendToClient.accept(reqKey, values.get(reqKey));
                }
            }
            return true;
        }

        // === 情况 B: 普通数据同步 ===
        if (!values.containsKey(key)) return false; // 未知 key，不处理

        values.put(key, val); // 更新本地

        // 特殊处理：如果是 C2S (客户端改的)，服务端收到后通常要广播回给客户端确认
        SyncMode mode = modes.get(key);
        if (sendToClient != null && (mode == SyncMode.C2S || mode == SyncMode.BOTH)) {
            sendToClient.accept(key, val);
        }

        return true;
    }
}
