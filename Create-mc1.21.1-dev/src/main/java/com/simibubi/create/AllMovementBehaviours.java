package com.simibubi.create;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.BellMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.CampfireMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.dispenser.DispenserMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.dispenser.DropperMovementBehaviour;

import net.minecraft.world.level.block.Blocks;

public class AllMovementBehaviours {
	static void registerDefaults() {
		MovementBehaviour.REGISTRY.register(Blocks.BELL, new BellMovementBehaviour());
		MovementBehaviour.REGISTRY.register(Blocks.CAMPFIRE, new CampfireMovementBehaviour());
		MovementBehaviour.REGISTRY.register(Blocks.SOUL_CAMPFIRE, new CampfireMovementBehaviour());
		MovementBehaviour.REGISTRY.register(Blocks.DISPENSER, new DispenserMovementBehaviour());
		MovementBehaviour.REGISTRY.register(Blocks.DROPPER, new DropperMovementBehaviour());
	}
}
