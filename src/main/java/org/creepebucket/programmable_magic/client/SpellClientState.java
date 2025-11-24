package org.creepebucket.programmable_magic.client;

/**
 * 客户端 HUD 状态：用于在 GUI（如 WandScreen）显示计算错误等信息，
 * 避免将错误通过聊天刷屏。
 */
public final class SpellClientState {
    private SpellClientState() {}

    private static volatile String computeError;
    private static volatile long computeErrorAt;

    public static void setComputeError(String message) {
        computeError = message;
        computeErrorAt = System.currentTimeMillis();
    }

    public static String pollComputeError(long ttlMillis) {
        if (computeError == null) return null;
        long now = System.currentTimeMillis();
        if (ttlMillis > 0 && now - computeErrorAt > ttlMillis) {
            computeError = null;
            return null;
        }
        return computeError;
    }

    public static void clearComputeError() { computeError = null; }
}

