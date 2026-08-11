package org.creepebucket.programmable_magic.mananet.connectors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.registries.ModAttachments;

import java.util.function.Supplier;

public abstract class AbstractManaConnector extends Block implements EntityBlock {
	public final Supplier<BlockEntityType<NetNodeBlockEntity>> blockEntityType;

	public AbstractManaConnector(Properties properties, Supplier<BlockEntityType<NetNodeBlockEntity>> blockEntityType) {
		super(properties);
		this.blockEntityType = blockEntityType;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new NetNodeBlockEntity(blockEntityType.get(), blockPos, blockState);
	}

	protected abstract boolean isValidConnectionFace(Direction face);

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.isClientSide()) return InteractionResult.SUCCESS;

		var hitLoc = hitResult.getLocation();
		var clickedFace = Direction.getApproximateNearest(hitLoc.x - pos.getX() - 0.5, hitLoc.y - pos.getY() - 0.5, hitLoc.z - pos.getZ() - 0.5);
		if (!isValidConnectionFace(clickedFace)) {
			player.sendSystemMessage(Component.translatable("message.programmable_magic.connector.reject_face"));
			return InteractionResult.CONSUME;
		}

		if (player.hasData(ModAttachments.PENDING_CONNECTION)) {
			var connectedPos = player.getData(ModAttachments.PENDING_CONNECTION);
			var connectedFace = player.getData(ModAttachments.PENDING_FACE);
			var selfFace = clickedFace;
			((NetNodeBlockEntity) level.getBlockEntity(pos)).connect(level, connectedPos, connectedFace, selfFace);

			player.removeData(ModAttachments.PENDING_CONNECTION);
			player.removeData(ModAttachments.PENDING_FACE);
			player.sendSystemMessage(Component.translatable("message.programmable_magic.connector.connected", connectedPos.toShortString(), connectedFace.getName(), pos.toShortString(), selfFace.getName()));
			return InteractionResult.CONSUME;
		}

		player.setData(ModAttachments.PENDING_CONNECTION, pos);
		player.setData(ModAttachments.PENDING_FACE, clickedFace);
		player.sendSystemMessage(Component.translatable("message.programmable_magic.connector.pending_set", pos.toShortString(), clickedFace.getName()));
		return InteractionResult.CONSUME;
	}
}
