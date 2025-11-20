package com.simibubi.create.content.contraptions.behaviour.dispenser;

import com.simibubi.create.api.contraption.dispenser.DefaultMountedDispenseBehavior;
import com.simibubi.create.api.contraption.dispenser.MountedDispenseBehavior;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public class DispenserMovementBehaviour extends DropperMovementBehaviour {
	@Override
	protected MountedDispenseBehavior getDispenseBehavior(MovementContext context, BlockPos pos, ItemStack stack) {
		MountedDispenseBehavior behavior = MountedDispenseBehavior.REGISTRY.get(stack.getItem());
		return behavior != null ? behavior : DefaultMountedDispenseBehavior.INSTANCE;
	}
}
