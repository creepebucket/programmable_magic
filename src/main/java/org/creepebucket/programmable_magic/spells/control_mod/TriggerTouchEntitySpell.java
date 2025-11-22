package org.creepebucket.programmable_magic.spells.control_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.creepebucket.programmable_magic.spells.SpellData;

import java.util.ArrayList;
import java.util.List;

public class TriggerTouchEntitySpell extends BaseControlModLogic {
    @Override
    public String getRegistryName() { return "trigger_touch_entity"; }

    @Override
    public boolean run(Player player, SpellData data) {
        if (data == null) return true;
        Entity carrier = data.getTarget();
        if (carrier == null) return true; // 无载体则不等待

        Level level = carrier.level();
        AABB aabb = carrier.getBoundingBox().inflate(0.3);

        // 查找周围实体（排除自身）
        List<Entity> list = level.getEntities(carrier, aabb, e -> e != carrier && e.isAlive());
        if (!list.isEmpty()) {
            // 选择最近实体
            Entity target = list.get(0);
            double bestD = target.distanceToSqr(carrier);
            for (int i = 1; i < list.size(); i++) {
                double d = list.get(i).distanceToSqr(carrier);
                if (d < bestD) { bestD = d; target = list.get(i); }
            }

            // 传送到实体位置（中心偏上），并清空速度，避免过快
            double nx = target.getX();
            double ny = target.getY() + target.getBbHeight() * 0.5;
            double nz = target.getZ();
            carrier.setPos(nx, ny, nz);
            return true; // 同刻继续执行下一个法术
        }
        return false; // 未找到则等待下一tick
    }

    @Override
    public void calculateBaseMana(SpellData data) { /* 控制法术不消耗魔力 */ }

    @Override
    public void applyManaModification(SpellData data) { /* 不修改耗魔 */ }

    @Override
    public List<Component> getTooltip() {
        List<Component> t = new ArrayList<>();
        t.add(Component.translatable("tooltip.programmable_magic.spell_modifier"));
        t.add(Component.translatable("tooltip.programmable_magic.trigger_touch_entity"));
        return t;
    }
}
