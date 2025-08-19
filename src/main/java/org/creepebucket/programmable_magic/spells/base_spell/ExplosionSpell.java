package org.creepebucket.programmable_magic.spells.base_spell;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;

import java.util.ArrayList;
import java.util.List;

public class ExplosionSpell extends BaseSpellEffectLogic {
    
    public ExplosionSpell() {
        super();
    }

    @Override
    public String getRegistryName() {
        return "explosion";
    }

    @Override
    public boolean run(Player player, SpellData data) {
        Level level = player.level();
        
        // 在法术位置创建爆炸
        level.explode(
                player,                           // 爆炸源
                data.getPosition().x,             // X坐标
                data.getPosition().y,             // Y坐标
                data.getPosition().z,             // Z坐标
                (float) (2.0 * data.getPower()), // 爆炸威力
                Level.ExplosionInteraction.TNT   // 爆炸类型
        );
        
        // 爆炸后法术结束
        data.setActive(false);
        return true;
    }
    
    @Override
    public void calculateBaseMana(SpellData data) {
        // 爆炸法术消耗压力系魔力
        data.setManaCost("pressure", 5.0);
        data.setManaCost("temperature", 2.0);
    }

    @Override
    public List<Component> getTooltip() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("tooltip.programmable_magic.mana_cost"));
        tooltip.add(Component.literal("  Pressure: 5.0"));
        tooltip.add(Component.literal("  Temperature: 2.0"));
        return tooltip;
    }
} 