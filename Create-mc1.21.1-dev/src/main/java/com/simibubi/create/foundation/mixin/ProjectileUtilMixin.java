package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {
	@WrapOperation(method = "getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;canRiderInteract()Z"))
	private static boolean create$interactWithEntitiesOnContraptions(Entity instance, Operation<Boolean> original) {
		return original.call(instance) || instance.getRootVehicle() instanceof AbstractContraptionEntity;
	}
}
