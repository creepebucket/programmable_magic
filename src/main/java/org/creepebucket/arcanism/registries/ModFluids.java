package org.creepebucket.arcanism.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

import static org.creepebucket.arcanism.Arcanism.MODID;

public class ModFluids {
	public static final DeferredRegister<FluidType> FLUID_TYPES =
			DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, MODID);
	public static final DeferredRegister<Fluid> FLUIDS =
			DeferredRegister.create(Registries.FLUID, MODID);

	public static final Supplier<FluidType> STEAM_FLUID_TYPE = FLUID_TYPES.register("steam",
			() -> new FluidType(FluidType.Properties.create()
					.descriptionId("fluid_type.arcanism.steam")
					.density(-100)
					.viscosity(200)
					.temperature(400)
					.canSwim(false)
					.canDrown(false)
					.canPushEntity(false)
					.supportsBoating(false)
					.fallDistanceModifier(1F)
					.pathType(null)
					.adjacentPathType(null)
					.motionScale(0.002D)
			));

	public static final Supplier<Fluid> STEAM_SOURCE = FLUIDS.register("steam",
			resourceKey -> new BaseFlowingFluid.Source(ModFluids.steamProperties()));
	public static final Supplier<Fluid> STEAM_FLOWING = FLUIDS.register("flowing_steam",
			resourceKey -> new BaseFlowingFluid.Flowing(ModFluids.steamProperties()));

	public static void register(IEventBus bus) {
		FLUID_TYPES.register(bus);
		FLUIDS.register(bus);
	}

	static BaseFlowingFluid.Properties steamProperties() {
		return new BaseFlowingFluid.Properties(STEAM_FLUID_TYPE, STEAM_SOURCE, STEAM_FLOWING);
	}
}
