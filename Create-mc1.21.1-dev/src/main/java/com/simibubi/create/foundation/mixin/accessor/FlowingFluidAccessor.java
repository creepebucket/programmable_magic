package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;

@Mixin(FlowingFluid.class)
public interface FlowingFluidAccessor {
	@Invoker("getNewLiquid")
	FluidState create$getNewLiquid(Level pLevel, BlockPos pPos, BlockState pBlockState);
}
