package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.mananet.machines.BasicMachine;
import org.creepebucket.programmable_magic.mananet.machines.DummyBlock;
import org.creepebucket.programmable_magic.mananet.machines.DummyBlockEntity;
import org.creepebucket.programmable_magic.mananet.machines.IMachineIo;
import org.creepebucket.programmable_magic.mananet.machines.consumer.water_pump.WaterPump;
import org.creepebucket.programmable_magic.mananet.machines.generator.solar_panel.SolarPanel;
import org.creepebucket.programmable_magic.mananet.machines.generator.wind_turbine.WindTurbine;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class MananetNodeBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredBlock<WindTurbine> WIND_TURBINE =
            BLOCKS.register("wind_turbine", registryName -> new WindTurbine(
                    BlockBehaviour.Properties.of().noOcclusion().setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredBlock<SolarPanel> SOLAR_PANEL =
            BLOCKS.register("solar_panel", registryName -> new SolarPanel(
                    BlockBehaviour.Properties.of().noOcclusion().setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredBlock<WaterPump> WATER_PUMP =
            BLOCKS.register("water_pump", registryName -> new WaterPump(
                    BlockBehaviour.Properties.of().noOcclusion().setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredBlock<DummyBlock> DUMMY_BLOCK =
            BLOCKS.register("dummy_block", registryName -> new DummyBlock(
                    BlockBehaviour.Properties.of().noOcclusion().instabreak().noLootTable().pushReaction(PushReaction.BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredBlock<DummyBlock.IODummyBlock> IO_DUMMY_BLOCK =
            BLOCKS.register("io_dummy_block", registryName -> new DummyBlock.IODummyBlock(
                    BlockBehaviour.Properties.of().noOcclusion().instabreak().noLootTable().pushReaction(PushReaction.BLOCK).setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredItem<BlockItem> WIND_TURBINE_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(WIND_TURBINE);

    public static final DeferredItem<BlockItem> SOLAR_PANEL_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(SOLAR_PANEL);

    public static final DeferredItem<BlockItem> WATER_PUMP_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(WATER_PUMP);

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        bus.addListener(RegisterCapabilitiesEvent.class, event -> {
            event.registerBlock(
                    Capabilities.Item.BLOCK,
                    (level, pos, state, blockEntity, direction) -> {
                        if (!(blockEntity instanceof DummyBlockEntity dummy_be)) return null;
                        var main_pos = DummyBlock.get_main_pos(pos, state);
                        if (level.getBlockState(main_pos).isAir()) return null;
                        var main_be = level.getBlockEntity(main_pos);
                        if (dummy_be.ioType == DummyBlockEntity.IoType.ITEM_INPUT && main_be instanceof IMachineIo.ItemInput io)
                            return io.getItemInput();
                        if (dummy_be.ioType == DummyBlockEntity.IoType.ITEM_OUTPUT && main_be instanceof IMachineIo.ItemOutput io)
                            return io.getItemOutput();
                        return null;
                    },
                    IO_DUMMY_BLOCK.get()
            );

            event.registerBlock(
                    Capabilities.Fluid.BLOCK,
                    (level, pos, state, blockEntity, direction) -> {
                        if (!(blockEntity instanceof DummyBlockEntity dummy_be)) return null;
                        var main_pos = DummyBlock.get_main_pos(pos, state);
                        if (level.getBlockState(main_pos).isAir()) return null;
                        var main_be = level.getBlockEntity(main_pos);
                        if (dummy_be.ioType == DummyBlockEntity.IoType.FLUID_INPUT && main_be instanceof IMachineIo.FluidInput io)
                            return io.getFluidInput();
                        if (dummy_be.ioType == DummyBlockEntity.IoType.FLUID_OUTPUT && main_be instanceof IMachineIo.FluidOutput io)
                            return io.getFluidOutput();
                        return null;
                    },
                    IO_DUMMY_BLOCK.get()
            );

            event.registerBlock(
                    Capabilities.Item.BLOCK,
                    (level, pos, state, be, direction) -> {
                        if (!(state.getBlock() instanceof BasicMachine machine)) return null;
                        for (var entry : machine.IO_ENTRIES) {
                            if (!entry.offset().equals(BlockPos.ZERO)) continue;
                            if (entry.ioType() == DummyBlockEntity.IoType.ITEM_INPUT && be instanceof IMachineIo.ItemInput io)
                                return io.getItemInput();
                            if (entry.ioType() == DummyBlockEntity.IoType.ITEM_OUTPUT && be instanceof IMachineIo.ItemOutput io)
                                return io.getItemOutput();
                        }
                        return null;
                    },
                    WIND_TURBINE.get(), SOLAR_PANEL.get(), WATER_PUMP.get()
            );

            event.registerBlock(
                    Capabilities.Fluid.BLOCK,
                    (level, pos, state, be, direction) -> {
                        if (!(state.getBlock() instanceof BasicMachine machine)) return null;
                        for (var entry : machine.IO_ENTRIES) {
                            if (!entry.offset().equals(BlockPos.ZERO)) continue;
                            if (entry.ioType() == DummyBlockEntity.IoType.FLUID_INPUT && be instanceof IMachineIo.FluidInput io)
                                return io.getFluidInput();
                            if (entry.ioType() == DummyBlockEntity.IoType.FLUID_OUTPUT && be instanceof IMachineIo.FluidOutput io)
                                return io.getFluidOutput();
                        }
                        return null;
                    },
                    WIND_TURBINE.get(), SOLAR_PANEL.get(), WATER_PUMP.get()
            );
        });
    }
}
