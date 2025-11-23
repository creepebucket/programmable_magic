package org.creepebucket.programmable_magic.spells.control_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import org.creepebucket.programmable_magic.spells.SpellData;

import java.util.ArrayList;
import java.util.List;

public class TriggerTouchGroundSpell extends BaseControlModLogic {
    @Override
    public String getRegistryName() { return "trigger_touch_ground"; }

    @Override
    public boolean run(Player player, SpellData data) {
        if (data == null) return true;
        Entity carrier = data.getTarget();
        if (carrier == null) return true; // 无载体则不等待

        Level level = carrier.level();
        AABB box = carrier.getBoundingBox().inflate(0.3);

        int minX = Mth.floor(box.minX);
        int maxX = Mth.floor(box.maxX);
        int minY = Mth.floor(box.minY);
        int maxY = Mth.floor(box.maxY);
        int minZ = Mth.floor(box.minZ);
        int maxZ = Mth.floor(box.maxZ);

        BlockPos best = null;
        int bestY = Integer.MIN_VALUE;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!level.isEmptyBlock(pos)) {
                        BlockState state = level.getBlockState(pos);
                        if (!state.isAir()) {
                            if (y > bestY) {
                                bestY = y;
                                best = pos.immutable();
                            }
                        }
                    }
                }
            }
        }

        if (best != null) {
            // 向上寻找最近的空气位置
            BlockPos target = best.above();
            int up = 0;
            while (up < 8 && !level.isEmptyBlock(target)) {
                target = target.above();
                up++;
            }
            // 传送至方块中心上方，清空速度，避免过冲
            double nx = target.getX() + 0.5;
            double ny = target.getY() + 0.5; // 微小抬升避免再次判定碰撞
            double nz = target.getZ() + 0.5;
            carrier.setPos(nx, ny, nz);
            return true; // 同刻继续执行下一个法术
        }

        // 未满足条件：等待下一tick再判断
        return false;
    }

    @Override
    public void calculateBaseMana(SpellData data) { /* 控制法术不消耗魔力 */ }

    @Override
    public void applyManaModification(SpellData data) { /* 不修改耗魔 */ }

    @Override
    public List<Component> getTooltip() {
        java.util.List<org.creepebucket.programmable_magic.spells.SpellValueType> in = new java.util.ArrayList<>();
        in.add(org.creepebucket.programmable_magic.spells.SpellValueType.MODIFIER);
        org.creepebucket.programmable_magic.spells.SpellValueType out = org.creepebucket.programmable_magic.spells.SpellValueType.MODIFIER;
        Component desc = Component.translatable("tooltip.programmable_magic.trigger_touch_ground");
        return org.creepebucket.programmable_magic.spells.SpellTooltipUtil.buildTooltip(in, out, desc, this);
    }
}
