package org.creepebucket.programmable_magic.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.creepebucket.programmable_magic.registries.ModItems;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@EventBusSubscriber(modid = MODID)
public class HandlerRightClickCauldronPurifyRedstone {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        var player = event.getEntity();
        var stack = event.getItemStack();
        var level = event.getLevel();
        BlockPos pos = event.getPos();

        // 需要：手持红石粉，右击水炼药锅，且其下方是火（或灵魂火）
        if (stack.is(Items.REDSTONE) && level.getBlockState(pos).is(Blocks.WATER_CAULDRON)) {
            BlockPos below = pos.below();
            BlockState belowState = level.getBlockState(below);
            boolean hasFireBelow = belowState.is(Blocks.FIRE) || belowState.is(Blocks.SOUL_FIRE);
            if (!hasFireBelow) return;

            if (!level.isClientSide) {
                ServerLevel sl = (ServerLevel) level;
                BlockState cauldron = level.getBlockState(pos);
                int lvl = cauldron.getValue(LayeredCauldronBlock.LEVEL);

                // 降低水位，若为 1 则变为空炼药锅
                if (lvl > 1) {
                    level.setBlock(pos, cauldron.setValue(LayeredCauldronBlock.LEVEL, lvl - 1), 3);
                } else {
                    level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
                }

                if (!player.isCreative()) {
                    stack.shrink(1);
                }

                // 生成“纯净红石粉”掉落，带一点上抛
                var pure = ModItems.PURE_REDSTONE_DUST.get().getDefaultInstance();
                ItemEntity drop = new ItemEntity(sl,
                        pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                        pure,
                        0.0, 0.2, 0.0);
                sl.addFreshEntity(drop);
            }

            event.setCanceled(true);
        }
    }
}

