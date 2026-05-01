package org.creepebucket.programmable_magic.mananet.mechines.wind_turbine;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.creepebucket.programmable_magic.gui.machines.WindTurbineMenu;
import org.creepebucket.programmable_magic.mananet.mechines.BasicMachine;
import org.jspecify.annotations.Nullable;

public class WindTurbine extends BasicMachine {

    public WindTurbine(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WindTurbineBlockEntity(pos, state);
    }

    public VoxelShape hitbox(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.422335, 2.25, 0.3125, 0.577665, 4.625, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.422335, 2.25, 0.3125, 0.577665, 4.625, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 2.25, 0.422335, 0.6875, 4.625, 0.577665), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 2.25, 0.422335, 0.6875, 4.625, 0.577665), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 4.5625, 0.375, 0.625, 4.6875, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.396446875, 1.5625, 0.25, 0.603553125, 2.25, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.396446875, 1.5625, 0.25, 0.603553125, 2.25, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 1.5625, 0.396446875, 0.75, 2.25, 0.603553125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 1.5625, 0.396446875, 0.75, 2.25, 0.603553125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1, 0, -1, 2, 0.1875, 2), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.25, 0.1875, -0.125, 1.25, 1.3750625, 1.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.125, 0.1875, -0.25, 1.125, 1.3750625, 1.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.125, 1.375, -0.125, 1.125, 1.5625, 1.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 4.6875, 0.3125, 0.6875, 5, 0.6875), BooleanOp.OR);

        return shape;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(state.getMenuProvider(level, pos), buf -> {
            });
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (containerId, inventory, p) -> new WindTurbineMenu(containerId, inventory),
                Component.literal("")
        );
    }
}
