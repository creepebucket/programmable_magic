package org.creepebucket.programmable_magic.mananet.machines;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.creepebucket.programmable_magic.gui.machines.io_dummy.IoDummyMenu;
import org.jspecify.annotations.Nullable;

public class IoDummyBlock extends DummyBlock implements EntityBlock {

    public IoDummyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new IoDummyBlockEntity(blockPos, blockState);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.isCrouching()) return super.useWithoutItem(state, level, pos, player, hitResult);
        return openMenu(level, pos, player);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.isCrouching()) return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        return openMenu(level, pos, player);
    }

	public InteractionResult openMenu(Level level, BlockPos pos, Player player) {
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			var blockEntity = (IoDummyBlockEntity) level.getBlockEntity(pos);
			var mainBlockEntity = (MachineBlockEntity) level.getBlockEntity(DummyBlock.get_main_pos(pos, level.getBlockState(pos)));
			var relativePos = blockEntity.getRelativePos();
			var handler = mainBlockEntity.itemStorage.containsKey(relativePos) ? mainBlockEntity.getItemHandler(relativePos) : mainBlockEntity.getFluidHandler(relativePos);
			var flowControl = (FlowControlHandler<?>) handler;
			serverPlayer.openMenu(new SimpleMenuProvider(
					(containerId, inventory, extra) -> new IoDummyMenu(containerId, inventory, pos, flowControl.handler, flowControl.canInput, flowControl.canOutput),
					Component.literal("")
			), buf -> {
				buf.writeBlockPos(pos);
				buf.writeBoolean(handler.getResource(0) instanceof ItemResource);
				buf.writeBoolean(flowControl.canInput);
				buf.writeBoolean(flowControl.canOutput);
			});
		}
		return InteractionResult.SUCCESS;
	}
}
