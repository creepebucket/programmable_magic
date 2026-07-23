package org.creepebucket.programmable_magic.mananet.machines;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;
import org.creepebucket.programmable_magic.utils.RelativeBlockPos;

public class IoDummyBlockEntity extends BlockEntity {
    public IoDummyBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.IO_DUMMY.get(), worldPosition, blockState);
    }

    public RelativeBlockPos getRelativePos() {
        var mainPos = DummyBlock.get_main_pos(getBlockPos(), getBlockState());
        var mainState = getLevel().getBlockState(mainPos);
        return RelativeBlockPos.fromAbsolutePos(getBlockPos().subtract(mainPos), mainState.getValue(BasicMachine.FACING));
    }

    public ResourceHandler<ItemResource> getItemHandler() {
        return ((MachineBlockEntity) getLevel().getBlockEntity(DummyBlock.get_main_pos(getBlockPos(), getBlockState()))).getItemHandler(getRelativePos());
    }

    public ResourceHandler<FluidResource> getFluidHandler() {
        return ((MachineBlockEntity) getLevel().getBlockEntity(DummyBlock.get_main_pos(getBlockPos(), getBlockState()))).getFluidHandler(getRelativePos());
    }

}
