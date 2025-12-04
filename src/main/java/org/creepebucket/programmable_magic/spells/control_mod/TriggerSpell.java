package org.creepebucket.programmable_magic.spells.control_mod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

public abstract class TriggerSpell extends BaseControlModLogic{

    public abstract boolean condition(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams);

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        // 触发器: 只有条件满足时才执行下一个法术

        if (this.getPrevSpell() instanceof ConditionInverter ^ condition(player, data, spellSequence, modifiers, spellParams)) {
            return Map.of("successful", true);
        } else {
            return Map.of("successful", false);
        }
    }

    @Override
    public void calculateBaseMana(SpellData data) {

    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() {
        return List.of(List.of(SpellValueType.EMPTY));
    }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() {
        return List.of(List.of(SpellValueType.MODIFIER));
    }

    public static class TriggerTouchGround extends TriggerSpell {

        @Override
        public boolean condition(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            // 检测当前BlockPos是否有方块

            Vec3i vec = new Vec3i(
                    Mth.floor(data.getPosition().x),
                    Mth.floor(data.getPosition().y),
                    Mth.floor(data.getPosition().z)
            );

            return !player.level().getBlockState(new BlockPos(vec)).isAir();
        }

        @Override
        public String getRegistryName() {
            return "trigger_touch_ground";
        }

        @Override
        public List<Component> getTooltip() {
            return List.of(
                    Component.translatable("tooltip.programmable_magic.spell.trigger_touch_ground.desc1"),
                    Component.translatable("tooltip.programmable_magic.spell.trigger_touch_ground.desc2")
            );
        }
    }

    public static class TriggerTouchEntity extends TriggerSpell {
        @Override
        public boolean condition(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            // 以当前法术位置为中心，检测极小范围内是否存在实体（排除自身与施法者）
            var pos = data.getPosition();
            var self = data.getCustomData("spell_entity", Entity.class);
            double r = 0.3; // 触碰半径
            AABB box = new AABB(pos.x - r, pos.y - r, pos.z - r, pos.x + r, pos.y + r, pos.z + r);

            List<Entity> found = player.level().getEntities((Entity) null, box, e -> e != null && e != self && e != player);
            return !found.isEmpty();
        }

        @Override
        public String getRegistryName() {
            return "trigger_touch_entity";
        }

        @Override
        public List<Component> getTooltip() {
            return List.of(
                    Component.translatable("tooltip.programmable_magic.spell.trigger_touch_entity.desc1"),
                    Component.translatable("tooltip.programmable_magic.spell.trigger_touch_entity.desc2")
            );
        }
    }
}
