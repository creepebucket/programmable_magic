package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(UseOnContext.class)
public interface UseOnContextAccessor {
	@Invoker("getHitResult")
	BlockHitResult create$getHitResult();
}
