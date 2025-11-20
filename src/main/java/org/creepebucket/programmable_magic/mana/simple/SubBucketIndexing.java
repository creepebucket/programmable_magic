package org.creepebucket.programmable_magic.mana.simple;

import net.minecraft.core.BlockPos;

/**
 * 子桶索引工具：把一个区块 Section(16x16x16) 切成 2x2x2 个 8x8x8 子桶。
 *
 * 目的：
 * - 在连通性计算时尽可能“局部化”，一次只在 8x8x8 的小体积内做 Flood Fill；
 * - 当越界到相邻子桶时，才把相邻子桶入队继续处理，避免扫整个 Section。
 *
 * 约定：
 * - 子桶基坐标为 8 的倍数（向下取整）；
 * - 子桶内的线性索引为 x*64 + y*8 + z，x/y/z ∈ [0,7]。
 */
public final class SubBucketIndexing {

    private SubBucketIndexing() {}

    /**
     * 计算所在 Section 坐标（三个 16 对齐的段坐标）。
     */
    public static SectionKey sectionOf(BlockPos pos) {
        return new SectionKey(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
    }

    /**
     * 计算 2x2x2 子桶索引 [0,7]，按 (sx<<2)|(sy<<1)|sz 打包。
     */
    public static int subBucketIndex(BlockPos pos) {
        int lx = pos.getX() & 15; // [0,15]
        int ly = pos.getY() & 15;
        int lz = pos.getZ() & 15;
        int sx = (lx >> 3) & 1;
        int sy = (ly >> 3) & 1;
        int sz = (lz >> 3) & 1;
        return (sx << 2) | (sy << 1) | sz;
    }

    /**
     * 计算该位置在 8x8x8 子桶中的线性索引 [0,511]，用于位集。
     */
    public static int indexInSubBucket(BlockPos pos) {
        int lx = pos.getX() & 7; // 以 8 为周期的局部坐标
        int ly = pos.getY() & 7;
        int lz = pos.getZ() & 7;
        return (lx << 6) | (ly << 3) | lz; // x*64 + y*8 + z
    }

    /** Section 键（16³ 对齐）。 */
    public static final class SectionKey {
        public final int sx, sy, sz;
        public SectionKey(int sx, int sy, int sz) { this.sx = sx; this.sy = sy; this.sz = sz; }
        @Override public int hashCode() { return (sx * 7340033) ^ (sy * 4099) ^ sz; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof SectionKey k)) return false;
            return sx == k.sx && sy == k.sy && sz == k.sz;
        }
        @Override public String toString() { return "SectionKey{"+sx+","+sy+","+sz+"}"; }
    }
}
