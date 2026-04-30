package org.creepebucket.programmable_magic.events.machines;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.mananet.mechines.BasicMachine;
import org.jspecify.annotations.Nullable;

public class BasicMachineItemHelper {

    public record Held(InteractionHand hand, ItemStack stack, BlockItem block_item) {
    }

    public static @Nullable Held get_held_basic_machine(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof BasicMachine) return new Held(InteractionHand.MAIN_HAND, stack, bi);

        stack = player.getOffhandItem();
        if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof BasicMachine) return new Held(InteractionHand.OFF_HAND, stack, bi);

        return null;
    }
}

