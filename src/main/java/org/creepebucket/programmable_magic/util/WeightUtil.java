package org.creepebucket.programmable_magic.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.creepebucket.programmable_magic.entities.SpellEntity;

public final class WeightUtil {
    private WeightUtil() {}

    public static double massForSpellEntity(SpellEntity se) {
        return se != null ? Math.max(0.0, se.getWeightKg()) : 10.0;
    }

    public static double massForEntity(Entity e) {
        if (e == null) return 100.0;
        // 特例：铁傀儡 10 吨
        if (e instanceof IronGolem) return 10_000.0;
        // 大型生物 / Boss：劫掠兽、凋零、监守者 -> 1 吨
        if (e instanceof Ravager || e instanceof WitherBoss || e instanceof Warden) return 1_000.0;
        // 普通被动生物/人形生物：100 kg
        if (e instanceof Player || e instanceof AbstractVillager || e instanceof Animal) return 100.0;
        // 兜底：100 kg
        return 100.0;
    }

    public static double massForProjectileItem(Item item) {
        if (item == null) return 0.0;
        // 简单映射：常见弹射物的质量（kg）
        if (item == Items.ARROW) return 0.03;            // 30 g
        if (item == Items.SPECTRAL_ARROW) return 0.03;
        if (item == Items.TIPPED_ARROW) return 0.03;
        if (item == Items.SNOWBALL) return 0.06;         // 60 g
        if (item == Items.EGG) return 0.05;              // 50 g
        if (item == Items.ENDER_PEARL) return 0.06;      // 60 g（估）
        if (item == Items.FIRE_CHARGE) return 0.02;      // 20 g（估）
        if (item == Items.TRIDENT) return 2.0;           // 2 kg（估）
        if (item == Items.POTION) return 0.30;           // 瓶/喷溅
        if (item == Items.SPLASH_POTION) return 0.30;
        if (item == Items.LINGERING_POTION) return 0.30;

        // 其它未知统一按 0.1 kg 处理
        return 0.1;
    }

    public static Item tryParseItem(Object any) {
        if (any instanceof Item i) return i;
        if (any instanceof net.minecraft.world.item.ItemStack st) return st.getItem();
        if (any instanceof CharSequence s) {
            ResourceLocation rl = ResourceLocation.tryParse(s.toString());
            if (rl != null) {
                var opt = BuiltInRegistries.ITEM.get(rl);
                return opt.map(Holder::value).orElse(null);
            }
        }
        if (any instanceof ResourceLocation rl) {
            var opt = BuiltInRegistries.ITEM.get(rl);
            return opt.map(Holder::value).orElse(null);
        }
        return null;
    }
}
