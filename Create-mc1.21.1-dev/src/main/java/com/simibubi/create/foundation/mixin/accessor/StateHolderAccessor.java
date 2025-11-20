package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.state.StateHolder;

@Mixin(StateHolder.class)
public interface StateHolderAccessor<O, S> {
	@Accessor
	O getOwner();
}
