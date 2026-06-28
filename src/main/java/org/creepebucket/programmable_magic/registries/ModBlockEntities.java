package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.mananet.machines.consumer.water_pump.WaterPumpBlockEntity;
import org.creepebucket.programmable_magic.mananet.machines.generator.solar_panel.SolarPanelBlockEntity;
import org.creepebucket.programmable_magic.mananet.machines.generator.wind_turbine.WindTurbineBlockEntity;

import java.util.function.Supplier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

    public static final Supplier<BlockEntityType<WindTurbineBlockEntity>> WIND_TURBINE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("wind_turbine", () -> new BlockEntityType<WindTurbineBlockEntity>(
                    WindTurbineBlockEntity::new, false, MananetNodeBlocks.WIND_TURBINE.get()));

    public static final Supplier<BlockEntityType<NetNodeBlockEntity>> BASIC_MANA_CONNECTOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("basic_mana_connector", () -> new BlockEntityType<NetNodeBlockEntity>(
                    NetNodeBlockEntity::new, false, ModBlocks.BASIC_MANA_CONNECTOR.get()));

    public static final Supplier<BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("solar_panel", () -> new BlockEntityType<SolarPanelBlockEntity>(
                    SolarPanelBlockEntity::new, false, MananetNodeBlocks.SOLAR_PANEL.get()));

    public static final Supplier<BlockEntityType<WaterPumpBlockEntity>> WATER_PUMP_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("water_pump", () -> new BlockEntityType<WaterPumpBlockEntity>(
                    WaterPumpBlockEntity::new, false, MananetNodeBlocks.WATER_PUMP.get()));

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
