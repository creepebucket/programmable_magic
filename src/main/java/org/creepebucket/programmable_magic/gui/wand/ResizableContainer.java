package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.world.SimpleContainer;

/**
 * 可调容量容器：
 * - 以最大容量初始化，运行时按插件数值设置“有效容量”。
 * - getContainerSize 返回有效容量，内部存储保留最大容量。
 */
public class ResizableContainer extends SimpleContainer {
    private final int max;
    private int limit;

    public ResizableContainer(int max) {
        super(max);
        this.max = Math.max(0, max);
        this.limit = this.max;
    }

    public void setLimit(int limit) {
        if (limit < 0) limit = 0;
        if (limit > max) limit = max;
        this.limit = limit;
    }

    @Override
    public int getContainerSize() {
        return this.limit;
    }
}

