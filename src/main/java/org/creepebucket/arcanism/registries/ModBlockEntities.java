package org.creepebucket.arcanism.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.arcanism.mananet.NetNodeBlockEntity;
import org.creepebucket.arcanism.mananet.machines.IoDummyBlockEntity;
import org.creepebucket.arcanism.mananet.machines.buffer.LargeManaBufferBlockEntity;
import org.creepebucket.arcanism.mananet.machines.buffer.MediumManaBufferBlockEntity;
import org.creepebucket.arcanism.mananet.machines.buffer.SmallManaBufferBlockEntity;
import org.creepebucket.arcanism.mananet.machines.consumer.liquid_heater.LiquidHeaterBlockEntity;
import org.creepebucket.arcanism.mananet.machines.consumer.water_pump.WaterPumpBlockEntity;
import org.creepebucket.arcanism.mananet.machines.generator.heat_exchanger.HeatExchangerBlockEntity;
import org.creepebucket.arcanism.mananet.machines.generator.pressure_relief_valve.PressureReliefValveBlockEntity;
import org.creepebucket.arcanism.mananet.machines.generator.solar_panel.SolarPanelBlockEntity;
import org.creepebucket.arcanism.mananet.machines.generator.steam_turbine.SteamTurbineBlockEntity;
import org.creepebucket.arcanism.mananet.machines.generator.wind_turbine.WindTurbineBlockEntity;

import java.util.function.Supplier;

import static org.creepebucket.arcanism.Arcanism.MODID;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

	public static final Supplier<BlockEntityType<WindTurbineBlockEntity>> WIND_TURBINE_BLOCK_ENTITY =
			BLOCK_ENTITIES.register("wind_turbine", () -> new BlockEntityType<WindTurbineBlockEntity>(
					WindTurbineBlockEntity::new, false, MananetNodeBlocks.WIND_TURBINE.get()));

	public static final Supplier<BlockEntityType<HeatExchangerBlockEntity>> HEAT_EXCHANGER_BLOCK_ENTITY =
			BLOCK_ENTITIES.register("heat_exchanger", () -> new BlockEntityType<HeatExchangerBlockEntity>(
					HeatExchangerBlockEntity::new, false, MananetNodeBlocks.HEAT_EXCHANGER.get()));

	public static final Supplier<BlockEntityType<SteamTurbineBlockEntity>> STEAM_TURBINE_BLOCK_ENTITY =
			BLOCK_ENTITIES.register("steam_turbine", () -> new BlockEntityType<SteamTurbineBlockEntity>(
					SteamTurbineBlockEntity::new, false, MananetNodeBlocks.STEAM_TURBINE.get()));

	public static final Supplier<BlockEntityType<PressureReliefValveBlockEntity>> PRESSURE_RELIEF_VALVE_BLOCK_ENTITY =
			BLOCK_ENTITIES.register("pressure_relief_valve", () -> new BlockEntityType<PressureReliefValveBlockEntity>(
					PressureReliefValveBlockEntity::new, false, MananetNodeBlocks.PRESSURE_RELIEF_VALVE.get()));

	public static final Supplier<BlockEntityType<NetNodeBlockEntity>> BASIC_MANA_CONNECTOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("basic_mana_connector", () -> new BlockEntityType<NetNodeBlockEntity>(
                    NetNodeBlockEntity::new, false, ModBlocks.BASIC_MANA_CONNECTOR.get()));

    public static final Supplier<BlockEntityType<NetNodeBlockEntity>> HORIZONTAL_MANA_CONNECTOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("horizontal_mana_connector", () -> new BlockEntityType<NetNodeBlockEntity>(
                    NetNodeBlockEntity::new, false, ModBlocks.HORIZONTAL_MANA_CONNECTOR.get()));

    public static final Supplier<BlockEntityType<NetNodeBlockEntity>> VERTICAL_MANA_CONNECTOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("vertical_mana_connector", () -> new BlockEntityType<NetNodeBlockEntity>(
                    NetNodeBlockEntity::new, false, ModBlocks.VERTICAL_MANA_CONNECTOR.get()));

    public static final Supplier<BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("solar_panel", () -> new BlockEntityType<SolarPanelBlockEntity>(
                    SolarPanelBlockEntity::new, false, MananetNodeBlocks.SOLAR_PANEL.get()));

    public static final Supplier<BlockEntityType<WaterPumpBlockEntity>> WATER_PUMP_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("water_pump", () -> new BlockEntityType<WaterPumpBlockEntity>(
                    WaterPumpBlockEntity::new, false, MananetNodeBlocks.WATER_PUMP.get()));

    public static final Supplier<BlockEntityType<LiquidHeaterBlockEntity>> LIQUID_HEATER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("liquid_heater", () -> new BlockEntityType<LiquidHeaterBlockEntity>(
                    LiquidHeaterBlockEntity::new, false, MananetNodeBlocks.LIQUID_HEATER.get()));

    public static final Supplier<BlockEntityType<SmallManaBufferBlockEntity>> SMALL_MANA_BUFFER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("small_mana_buffer", () -> new BlockEntityType<SmallManaBufferBlockEntity>(
                    SmallManaBufferBlockEntity::new, false, MananetNodeBlocks.SMALL_MANA_BUFFER.get()));

    public static final Supplier<BlockEntityType<MediumManaBufferBlockEntity>> MEDIUM_MANA_BUFFER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("medium_mana_buffer", () -> new BlockEntityType<MediumManaBufferBlockEntity>(
                    MediumManaBufferBlockEntity::new, false, MananetNodeBlocks.MEDIUM_MANA_BUFFER.get()));

    public static final Supplier<BlockEntityType<LargeManaBufferBlockEntity>> LARGE_MANA_BUFFER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("large_mana_buffer", () -> new BlockEntityType<LargeManaBufferBlockEntity>(
                    LargeManaBufferBlockEntity::new, false, MananetNodeBlocks.LARGE_MANA_BUFFER.get()));

    public static final Supplier<BlockEntityType<IoDummyBlockEntity>> IO_DUMMY =
            BLOCK_ENTITIES.register("io_dummy", () -> new BlockEntityType<IoDummyBlockEntity>(
                    IoDummyBlockEntity::new, false, MananetNodeBlocks.IO_DUMMY_BLOCK.get()));


    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
