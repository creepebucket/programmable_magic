package org.creepebucket.programmable_magic.spells;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.jspecify.annotations.Nullable;

public class PackedSpell extends Item {
    public PackedSpell(Properties properties) {
        super(properties.component(ModDataComponents.RESOURCE_LOCATION, "item/packed_spell_default.png"));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);

        stack.set(ModDataComponents.RESOURCE_LOCATION, "item/spell/number_digit_" + Minecraft.getInstance().level.getGameTime() % 20 + ".png");
    }
}
