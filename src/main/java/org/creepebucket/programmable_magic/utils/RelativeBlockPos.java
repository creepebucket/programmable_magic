package org.creepebucket.programmable_magic.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class RelativeBlockPos {
    public static final RelativeBlockPos ZERO = new RelativeBlockPos(0, 0, 0);
    public int facing_off, y_off, cw90_off;

    public RelativeBlockPos(int facing_off, int y_off, int cw90_off) {
        this.facing_off = facing_off;
        this.y_off = y_off;
        this.cw90_off = cw90_off;
    }

    public static RelativeBlockPos fromAbsolutePos(BlockPos pos, Direction facing) {
        return switch (facing) {
            case SOUTH -> new RelativeBlockPos(pos.getZ(), pos.getY(), -pos.getX());
            case WEST -> new RelativeBlockPos(-pos.getX(), pos.getY(), -pos.getZ());
            case EAST -> new RelativeBlockPos(pos.getX(), pos.getY(), pos.getZ());
            default -> new RelativeBlockPos(-pos.getZ(), pos.getY(), pos.getX());
        };
    }

    public BlockPos facingNorth() {
        return new BlockPos(cw90_off, y_off, -facing_off);
    }

    public BlockPos facingSouth() {
        return new BlockPos(-cw90_off, y_off, facing_off);
    }

    public BlockPos facingWest() {
        return new BlockPos(-facing_off, y_off, -cw90_off);
    }

    public BlockPos facingEast() {
        return new BlockPos(facing_off, y_off, cw90_off);
    }

    public BlockPos toAbsolutePos(Direction facing) {
        return switch (facing) {
            case SOUTH -> facingSouth();
            case WEST -> facingWest();
            case EAST -> facingEast();
            default -> facingNorth();
        };
    }

    public long asLong() {
        long l = (long) (facing_off & 0x3FFFFFF) << 38;
        long m = (long) (y_off & 0xFFF) << 26;
        long n = cw90_off & 0x3FFFFFF;
        return l | m | n;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RelativeBlockPos other)) return false;
        return facing_off == other.facing_off && y_off == other.y_off && cw90_off == other.cw90_off;
    }

    @Override
    public int hashCode() {
        return (int) asLong();
    }
}
