package org.creepebucket.programmable_magic.registries;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.mananet.EnergyHatch;
import org.creepebucket.programmable_magic.mananet.ManaBufferBlock;
import org.creepebucket.programmable_magic.mananet.ManaCableBlock;
import org.creepebucket.programmable_magic.mananet.ManaGeneratorBlock;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class MananetNodeBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredBlock<ManaCableBlock> MANA_CABLE = ManaCableBlock.register(BLOCKS, ITEMS);
    public static final DeferredBlock<ManaGeneratorBlock> MANA_GENERATOR = ManaGeneratorBlock.register(BLOCKS, ITEMS);
    public static final DeferredBlock<ManaBufferBlock> MANA_BUFFER = ManaBufferBlock.register(BLOCKS, ITEMS);
    public static final DeferredBlock<EnergyHatch> ENERGY_HATCH_T1 = EnergyHatch.register(BLOCKS, ITEMS, 1);
    public static final DeferredBlock<EnergyHatch> ENERGY_HATCH_T2 = EnergyHatch.register(BLOCKS, ITEMS, 2);
    public static final DeferredBlock<EnergyHatch> ENERGY_HATCH_T3 = EnergyHatch.register(BLOCKS, ITEMS, 3);
    public static final DeferredBlock<EnergyHatch> ENERGY_HATCH_T4 = EnergyHatch.register(BLOCKS, ITEMS, 4);
    public static final DeferredBlock<EnergyHatch> ENERGY_HATCH_T5 = EnergyHatch.register(BLOCKS, ITEMS, 5);

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
