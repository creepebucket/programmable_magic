package org.creepebucket.programmable_magic.mananet.mechines;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * 纯ai
 */
public class DummyBlock extends Block {

    public static final SignedOffsetProperty X_OFFSET = SignedOffsetProperty.create("x_offset");
    public static final SignedOffsetProperty Y_OFFSET = SignedOffsetProperty.create("y_offset");
    public static final SignedOffsetProperty Z_OFFSET = SignedOffsetProperty.create("z_offset");

    public DummyBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(X_OFFSET, 0).setValue(Y_OFFSET, 0).setValue(Z_OFFSET, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(X_OFFSET, Y_OFFSET, Z_OFFSET);
    }

    public static BlockPos get_main_pos(BlockPos pos, BlockState state) {
        return pos.offset(state.getValue(X_OFFSET), state.getValue(Y_OFFSET), state.getValue(Z_OFFSET));
    }

    public static Vec3 get_offset_vec(BlockState state) {
        return new Vec3(state.getValue(X_OFFSET), state.getValue(Y_OFFSET), state.getValue(Z_OFFSET));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        var main_pos = get_main_pos(pos, state);
        var main_state = level.getBlockState(main_pos);
        var offset = get_offset_vec(state);
        var mapped_hit = new BlockHitResult(
                hitResult.getLocation().add(offset),
                hitResult.getDirection(),
                main_pos,
                hitResult.isInside(),
                hitResult.isWorldBorderHit()
        );
        return main_state.useWithoutItem(level, player, mapped_hit);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        var main_pos = get_main_pos(pos, state);
        var main_state = level.getBlockState(main_pos);
        var offset = get_offset_vec(state);
        var mapped_hit = new BlockHitResult(
                hitResult.getLocation().add(offset),
                hitResult.getDirection(),
                main_pos,
                hitResult.isInside(),
                hitResult.isWorldBorderHit()
        );
        return main_state.useItemOn(stack, level, player, hand, mapped_hit);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.gameMode.destroyBlock(get_main_pos(pos, state));
        }
        return state;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var main_pos = get_main_pos(pos, state);
        var main_state = level.getBlockState(main_pos);
        var offset = get_offset_vec(state);
        return main_state.getShape(level, main_pos, context).move(offset);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var main_pos = get_main_pos(pos, state);
        var main_state = level.getBlockState(main_pos);
        var offset = get_offset_vec(state);
        return main_state.getCollisionShape(level, main_pos, context).move(offset);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        var main_pos = get_main_pos(pos, state);
        var main_state = level.getBlockState(main_pos);
        return main_state.getBlock().getCloneItemStack(level, main_pos, main_state, includeData, player);
    }

    public static final class SignedOffsetProperty extends Property<Integer> {

        public static final List<Integer> VALUES = List.of(-4, -3, -2, -1, 0, 1, 2, 3, 4);

        private SignedOffsetProperty(String name) {
            super(name, Integer.class);
        }

        public static SignedOffsetProperty create(String name) {
            return new SignedOffsetProperty(name);
        }

        @Override
        public List<Integer> getPossibleValues() {
            return VALUES;
        }

        @Override
        public String getName(Integer value) {
            if (value < 0) {
                return "neg" + -value;
            }
            return value.toString();
        }

        @Override
        public Optional<Integer> getValue(String value) {
            try {
                if (value.startsWith("neg")) {
                    int parsed = Integer.parseInt(value.substring(3));
                    int actual = -parsed;
                    return actual >= -4 && actual <= 4 ? Optional.of(actual) : Optional.empty();
                }
                int parsed = Integer.parseInt(value);
                return parsed >= -4 && parsed <= 4 ? Optional.of(parsed) : Optional.empty();
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }

        @Override
        public int getInternalIndex(Integer value) {
            return value >= -4 && value <= 4 ? value + 4 : -1;
        }
    }
}
