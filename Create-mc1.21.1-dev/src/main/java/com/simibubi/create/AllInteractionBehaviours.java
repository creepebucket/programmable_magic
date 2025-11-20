package com.simibubi.create;

import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.content.contraptions.behaviour.DoorMovingInteraction;
import com.simibubi.create.content.contraptions.behaviour.LeverMovingInteraction;
import com.simibubi.create.content.contraptions.behaviour.TrapdoorMovingInteraction;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

public class AllInteractionBehaviours {
	static void registerDefaults() {
		MovingInteractionBehaviour.REGISTRY.register(Blocks.LEVER, new LeverMovingInteraction());

		MovingInteractionBehaviour.REGISTRY.registerProvider(SimpleRegistry.Provider.forBlockTag(BlockTags.WOODEN_DOORS, new DoorMovingInteraction()));
		MovingInteractionBehaviour.REGISTRY.registerProvider(SimpleRegistry.Provider.forBlockTag(BlockTags.WOODEN_TRAPDOORS, new TrapdoorMovingInteraction()));
		MovingInteractionBehaviour.REGISTRY.registerProvider(SimpleRegistry.Provider.forBlockTag(BlockTags.FENCE_GATES, new TrapdoorMovingInteraction()));
	}
}
