package org.creepebucket.arcanism.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.arcanism.mananet.machines.DummyBlock;
import org.creepebucket.arcanism.mananet.machines.IoDummyBlock;
import org.creepebucket.arcanism.mananet.machines.buffer.LargeManaBuffer;
import org.creepebucket.arcanism.mananet.machines.buffer.MediumManaBuffer;
import org.creepebucket.arcanism.mananet.machines.buffer.SmallManaBuffer;
import org.creepebucket.arcanism.mananet.machines.consumer.liquid_heater.LiquidHeater;
import org.creepebucket.arcanism.mananet.machines.consumer.water_pump.WaterPump;
import org.creepebucket.arcanism.mananet.machines.generator.heat_exchanger.HeatExchanger;
import org.creepebucket.arcanism.mananet.machines.generator.pressure_relief_valve.PressureReliefValve;
import org.creepebucket.arcanism.mananet.machines.generator.solar_panel.SolarPanel;
import org.creepebucket.arcanism.mananet.machines.generator.steam_turbine.SteamTurbine;
import org.creepebucket.arcanism.mananet.machines.generator.wind_turbine.WindTurbine;

import static org.creepebucket.arcanism.Arcanism.MODID;

public class MananetNodeBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredBlock<WindTurbine> WIND_TURBINE =
            BLOCKS.register("wind_turbine", registryName -> new WindTurbine(
                    BlockBehaviour.Properties.of().noOcclusion().setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredBlock<SolarPanel> SOLAR_PANEL =
            BLOCKS.register("solar_panel", registryName -> new SolarPanel(
                    BlockBehaviour.Properties.of().noOcclusion().setId(ResourceKey.create(Registries.BLOCK, registryName))));

	public static final DeferredBlock<HeatExchanger> HEAT_EXCHANGER =
			BLOCKS.register("heat_exchanger", registryName -> new HeatExchanger(
					BlockBehaviour.Properties.of().noOcclusion().setId(ResourceKey.create(Registries.BLOCK, registryName))));

	public static final DeferredBlock<SteamTurbine> STEAM_TURBINE =
			BLOCKS.register("steam_turbine", registryName -> new SteamTurbine(
					BlockBehaviour.Properties.of().noOcclusion().setId(ResourceKey.create(Registries.BLOCK, registryName))));

	public static final DeferredBlock<PressureReliefValve> PRESSURE_RELIEF_VALVE =
			BLOCKS.register("pressure_relief_valve", registryName -> new PressureReliefValve(
					BlockBehaviour.Properties.of().noOcclusion().setId(ResourceKey.create(Registries.BLOCK, registryName))));

	public static final DeferredBlock<WaterPump> WATER_PUMP =
            BLOCKS.register("water_pump", registryName -> new WaterPump(
                    BlockBehaviour.Properties.of().noOcclusion().setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredBlock<LiquidHeater> LIQUID_HEATER =
            BLOCKS.register("liquid_heater", registryName -> new LiquidHeater(
                    BlockBehaviour.Properties.of().noOcclusion().setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredBlock<SmallManaBuffer> SMALL_MANA_BUFFER =
            BLOCKS.register("small_mana_buffer", registryName -> new SmallManaBuffer(
                    BlockBehaviour.Properties.of().noOcclusion().setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredBlock<MediumManaBuffer> MEDIUM_MANA_BUFFER =
            BLOCKS.register("medium_mana_buffer", registryName -> new MediumManaBuffer(
                    BlockBehaviour.Properties.of().noOcclusion().setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredBlock<LargeManaBuffer> LARGE_MANA_BUFFER =
            BLOCKS.register("large_mana_buffer", registryName -> new LargeManaBuffer(
                    BlockBehaviour.Properties.of().noOcclusion().setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredBlock<DummyBlock> DUMMY_BLOCK =
            BLOCKS.register("dummy_block", registryName -> new DummyBlock(
                    BlockBehaviour.Properties.of().noOcclusion().instabreak().noLootTable().pushReaction(PushReaction.BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredBlock<IoDummyBlock> IO_DUMMY_BLOCK =
            BLOCKS.register("io_dummy_block", registryName -> new IoDummyBlock(
                    BlockBehaviour.Properties.of().noOcclusion().instabreak().noLootTable().pushReaction(PushReaction.BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))));


    public static final DeferredItem<BlockItem> WIND_TURBINE_BLOCK_ITEM =
            registerMachineItem(WIND_TURBINE);

	public static final DeferredItem<BlockItem> SOLAR_PANEL_BLOCK_ITEM =
			registerMachineItem(SOLAR_PANEL);

	public static final DeferredItem<BlockItem> HEAT_EXCHANGER_BLOCK_ITEM =
			registerMachineItem(HEAT_EXCHANGER);

	public static final DeferredItem<BlockItem> STEAM_TURBINE_BLOCK_ITEM =
			registerMachineItem(STEAM_TURBINE);

	public static final DeferredItem<BlockItem> PRESSURE_RELIEF_VALVE_BLOCK_ITEM =
			registerMachineItem(PRESSURE_RELIEF_VALVE);

	public static final DeferredItem<BlockItem> WATER_PUMP_BLOCK_ITEM =
            registerMachineItem(WATER_PUMP);

    public static final DeferredItem<BlockItem> LIQUID_HEATER_BLOCK_ITEM =
            registerMachineItem(LIQUID_HEATER);

    public static final DeferredItem<BlockItem> SMALL_MANA_BUFFER_BLOCK_ITEM =
            registerMachineItem(SMALL_MANA_BUFFER);

    public static final DeferredItem<BlockItem> MEDIUM_MANA_BUFFER_BLOCK_ITEM =
            registerMachineItem(MEDIUM_MANA_BUFFER);

    public static final DeferredItem<BlockItem> LARGE_MANA_BUFFER_BLOCK_ITEM =
            registerMachineItem(LARGE_MANA_BUFFER);

    public static <B extends Block> DeferredItem<BlockItem> registerMachineItem(DeferredBlock<B> block) {
        return ITEMS.registerSimpleBlockItem(block);
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
