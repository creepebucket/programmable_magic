package com.simibubi.create;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.bearing.ClockworkContraption;
import com.simibubi.create.content.contraptions.bearing.StabilizedContraption;
import com.simibubi.create.content.contraptions.elevator.ElevatorContraption;
import com.simibubi.create.content.contraptions.gantry.GantryContraption;
import com.simibubi.create.content.contraptions.mounted.MountedContraption;
import com.simibubi.create.content.contraptions.piston.PistonContraption;
import com.simibubi.create.content.contraptions.pulley.PulleyContraption;
import com.simibubi.create.content.trains.entity.CarriageContraption;

import net.minecraft.core.Holder.Reference;
import net.minecraft.core.Registry;

public class AllContraptionTypes {
	public static final Map<String, ContraptionType> BY_LEGACY_NAME = new HashMap<>();

	public static final Reference<ContraptionType> PISTON = register("piston", PistonContraption::new);
	public static final Reference<ContraptionType> BEARING = register("bearing", BearingContraption::new);
	public static final Reference<ContraptionType> PULLEY = register("pulley", PulleyContraption::new);
	public static final Reference<ContraptionType> CLOCKWORK = register("clockwork", ClockworkContraption::new);
	public static final Reference<ContraptionType> MOUNTED = register("mounted", MountedContraption::new);
	public static final Reference<ContraptionType> STABILIZED = register("stabilized", StabilizedContraption::new);
	public static final Reference<ContraptionType> GANTRY = register("gantry", GantryContraption::new);
	public static final Reference<ContraptionType> CARRIAGE = register("carriage", CarriageContraption::new);
	public static final Reference<ContraptionType> ELEVATOR = register("elevator", ElevatorContraption::new);

	private static Reference<ContraptionType> register(String name, Supplier<? extends Contraption> factory) {
		ContraptionType type = new ContraptionType(factory);
		BY_LEGACY_NAME.put(name, type);

		return Registry.registerForHolder(CreateBuiltInRegistries.CONTRAPTION_TYPE, Create.asResource(name), type);
	}

	public static void init() {
	}
}
