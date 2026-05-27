package org.creepebucket.programmable_magic.mananet.mechines.solar_panel;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.creepebucket.programmable_magic.gui.machines.solar_panel.SolarPanelMenu;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.mananet.mechines.BasicMachine;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;
import org.jspecify.annotations.Nullable;

public class SolarPanel extends BasicMachine {

    public SolarPanel(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SolarPanelBlockEntity(pos, state);
    }

    public VoxelShape hitbox() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.4375, 0.5625, 0.4375, 0.5625, 1.875, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 0.5625, 0.30625, 0.6875, 0.8125, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 1.6875, 0.3125, 0.625, 2.0625, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.5625, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, -0.0625, 1, 0.3125, 1.0625), BooleanOp.OR);
        return shape;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(state.getMenuProvider(level, pos), buf -> {
                buf.writeBlockPos(pos);
            });
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (containerId, inventory, p) -> new SolarPanelMenu(containerId, inventory, pos),
                Component.literal("")
        );
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.SOLAR_PANEL_BLOCK_ENTITY.get()) {
            return (lvl, pos, st, blockEntity) -> SolarPanelBlockEntity.tick(lvl, pos, st, (SolarPanelBlockEntity) blockEntity);
        }
        return null;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        NetNodeBlockEntity.rebuildNetworkId(level, pos);
    }
}
