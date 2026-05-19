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

    public VoxelShape hitbox(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.4375, 0.5625, 0.4375, 0.5625, 1.1875, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.61875, 0.5625, 0.3125, 0.6875, 0.8125, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.38125, 0.5625, 0.325, 0.61875, 0.71875, 0.40625), BooleanOp.OR);

        shape = Shapes.join(shape, Shapes.box(0.4375, 1.1875, 0.4375, 0.5625, 1.6875, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 1.6875, 0.375, 0.625, 1.75, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 1.675, 0.3625, 0.6875, 1.975, 0.4375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.38125, 0.5625, 0.40625, 0.61875, 0.8, 0.59375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.38125, 0.5625, 0.59375, 0.61875, 0.71875, 0.675), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.38125, 0.64992375, 0.79633875, 0.61875, 0.76492375, 0.85883875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 1.8625, 0.45625, 0.625, 1.95, 0.54375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 1.675, 0.4375, 0.6875, 2.05, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 1.675, 0.5625, 0.6875, 1.975, 0.6375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 1.94596, 0.567390625, 0.6875, 2.05221, 0.623640625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 1.94375, 0.375444375, 0.6875, 2.05, 0.437944375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.38125, 0.64992375, 0.14116125000000002, 0.61875, 0.76492375, 0.20366125000000002), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 0.5625, 0.3125, 0.38125, 0.8125, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 1.94596, 0.567390625, 0.375, 2.05221, 0.623640625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 1.94375, 0.375444375, 0.375, 2.05, 0.437944375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 1.675, 0.4375, 0.375, 2.05, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 1.675, 0.5625, 0.375, 1.975, 0.6375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.5, 0.1875, 0.8125, 0.5, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.5625, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.16374999999999995, 0.007499999999999951, 0.00003124999999998268, 0.08625, 0.5075, 0.9999687500000001), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 1.675, 0.375, 0.375, 1.975, 0.45), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.5, 0.25, 0.75, 0.5625, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9025000000000001, 0.09187500000000004, 0.00003124999999998268, 1.1524999999999999, 0.591875, 0.9999687500000001), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.8125, 1, 0.5625, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.125, 0, 0, 1.125, 0.125, 1), BooleanOp.OR);

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
