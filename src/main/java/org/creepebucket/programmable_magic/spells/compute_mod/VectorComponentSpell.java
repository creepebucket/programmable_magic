package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.spells.SpellValueType.NUMBER;
import static org.creepebucket.programmable_magic.spells.SpellValueType.VECTOR3;

public abstract class VectorComponentSpell extends BaseComputeModLogic {
    protected double getComponent(Vec3 v) { return v.x; }
    protected String getTooltipKey() { return getRegistryName(); }

    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.compute_number"); }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        Vec3 v = (Vec3) spellParams.get(0);
        return Map.of("successful", true, "type", NUMBER, "value", getComponent(v));
    }

    @Override
    public List<Component> getTooltip() {
        String key = getTooltipKey();
        return List.of(
                Component.translatable("tooltip.programmable_magic.spell." + key + ".desc1"),
                Component.translatable("tooltip.programmable_magic.spell." + key + ".desc2")
        );
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() { return List.of(List.of(VECTOR3)); }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(NUMBER)); }

    public static class VecXSpell extends VectorComponentSpell {
        @Override
        public String getRegistryName() { return "compute_vec_x"; }

        @Override
        protected double getComponent(Vec3 v) { return v.x; }

        @Override
        protected String getTooltipKey() { return "vec_x"; }
    }

    public static class VecYSpell extends VectorComponentSpell {
        @Override
        public String getRegistryName() { return "compute_vec_y"; }

        @Override
        protected double getComponent(Vec3 v) { return v.y; }

        @Override
        protected String getTooltipKey() { return "vec_y"; }
    }

    public static class VecZSpell extends VectorComponentSpell {
        @Override
        public String getRegistryName() { return "compute_vec_z"; }

        @Override
        protected double getComponent(Vec3 v) { return v.z; }

        @Override
        protected String getTooltipKey() { return "vec_z"; }
    }
}
