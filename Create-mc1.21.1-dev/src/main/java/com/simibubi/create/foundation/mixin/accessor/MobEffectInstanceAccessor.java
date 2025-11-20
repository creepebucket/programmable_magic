package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.effect.MobEffectInstance;

@Mixin(MobEffectInstance.class)
public interface MobEffectInstanceAccessor {
	@Accessor("hiddenEffect")
	MobEffectInstance create$getHiddenEffect();
}
