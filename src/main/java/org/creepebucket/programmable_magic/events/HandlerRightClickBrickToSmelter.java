package org.creepebucket.programmable_magic.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.creepebucket.programmable_magic.registries.ModBlocks;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@EventBusSubscriber(modid = MODID)
public class HandlerRightClickBrickToSmelter {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        var player = event.getEntity();
        var stack = event.getItemStack();
        var level = event.getLevel();
        BlockPos pos = event.getPos();

        // 任意锄头 + 砖块
        if (!(stack.getItem() instanceof HoeItem)) return;
        if (!level.getBlockState(pos).is(Blocks.BRICKS)) return;

        if (!level.isClientSide) {
            // 替换为合金炉，默认 EMPTY 状态
            var state = ModBlocks.PRIMITIVE_ALLOY_SMELTER.get().defaultBlockState()
                    .setValue(org.creepebucket.programmable_magic.block.PrimitiveAlloySmelterBlock.FACING, player.getDirection().getOpposite());
            level.setBlock(pos, state, 3);

            // 返还一块“砖”（Items.BRICK）物品，若背包放不下则掉落
            ItemStack bricksOne = net.minecraft.world.item.Items.BRICK.getDefaultInstance();
            boolean added = player.addItem(bricksOne);
            if (!added && level instanceof ServerLevel sl) {
                ItemEntity drop = new ItemEntity(sl,
                        pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                        bricksOne,
                        0.0, 0.1, 0.0);
                sl.addFreshEntity(drop);
            }

            // 扣耐久（非创造），并播放放置音效
            if (!player.isCreative()) {
                // 1.21.8: 使用带 InteractionHand 的重载
                stack.hurtAndBreak(1, player, event.getHand());
            }
            level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.8f, 1.0f);
        }

        event.setCanceled(true);
    }
}
