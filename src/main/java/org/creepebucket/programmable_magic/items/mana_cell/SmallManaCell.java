package org.creepebucket.programmable_magic.items.mana_cell;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.creepebucket.programmable_magic.registries.ModDataComponents;

public class SmallManaCell extends BaseManaCell {
    public SmallManaCell(Properties properties) {
        super(properties, 100, 1);
    }

    @Override
    public InteractionResult use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        ItemStack stack = p_41433_.getItemInHand(p_41434_);
        if (!p_41432_.isClientSide()) {
            setMana(stack, "radiation", getMaxMana());
            setMana(stack, "temperature", getMaxMana());
            setMana(stack, "momentum", getMaxMana());
            setMana(stack, "pressure", getMaxMana());
        }
        return InteractionResult.SUCCESS;
    }
}
//