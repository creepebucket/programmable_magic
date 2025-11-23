package org.creepebucket.programmable_magic.spells.base_spell;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.spells.SpellData;

import java.util.ArrayList;
import java.util.List;

public class IgniteSpell extends BaseSpellEffectLogic {

    public IgniteSpell() {
        super();
    }

    @Override
    public String getRegistryName() {
        return "ignite";
    }

    @Override
    public boolean run(Player player, SpellData data) {
        Level level = player.level();
        BlockPos center = BlockPos.containing(data.getPosition());

        // 直径 = sqrt(power)，半径 = 直径 / 2
        double pow = Math.max(0.0, data.getPower());
        int radius = Math.max(0, Mth.floor(Math.sqrt(pow) / 2.0));

        boolean placedAny = false;
        if (radius <= 0) {
            if (level.isEmptyBlock(center) && !level.getBlockState(center.below()).isAir()) {
                level.setBlock(center, Blocks.FIRE.defaultBlockState(), 3);
                placedAny = true;
            }
        } else {
            int cx = center.getX();
            int cy = center.getY();
            int cz = center.getZ();
            int r2 = radius * radius;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz > r2) continue;
                    int wx = cx + dx;
                    int wz = cz + dz;

                    // 在近邻高度范围内寻找“表面上的空气方块”：该位置是空气，且其下方不是空气
                    for (int dy = 3; dy >= -3; dy--) {
                        BlockPos p = new BlockPos(wx, cy + dy, wz);
                        BlockPos below = p.below();
                        if (level.isEmptyBlock(p)) {
                            BlockState belowState = level.getBlockState(below);
                            if (!belowState.isAir()) {
                                level.setBlock(p, Blocks.FIRE.defaultBlockState(), 3);
                                placedAny = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        // 若未成功放置，尝试在当前位置直接点燃（最后兜底）
        if (!placedAny && level.isEmptyBlock(center)) {
            level.setBlock(center, Blocks.FIRE.defaultBlockState(), 3);
        }
        return true;
    }

    @Override
    public void calculateBaseMana(SpellData data) {
        data.setManaCost("temperature", 50.0);
    }

    @Override
    public List<Component> getTooltip() {
        java.util.List<org.creepebucket.programmable_magic.spells.SpellValueType> in = new java.util.ArrayList<>();
        in.add(org.creepebucket.programmable_magic.spells.SpellValueType.MODIFIER);
        org.creepebucket.programmable_magic.spells.SpellValueType out = org.creepebucket.programmable_magic.spells.SpellValueType.SPELL;
        var desc = net.minecraft.network.chat.Component.literal("点燃地面/附近可燃位置");
        return org.creepebucket.programmable_magic.spells.SpellTooltipUtil.buildTooltip(in, out, desc, this);
    }
}
