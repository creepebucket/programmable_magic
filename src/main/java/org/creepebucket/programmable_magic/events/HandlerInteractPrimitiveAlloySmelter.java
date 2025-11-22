package org.creepebucket.programmable_magic.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.creepebucket.programmable_magic.block.PrimitiveAlloySmelterBlock;
import org.creepebucket.programmable_magic.blockentity.PrimitiveAlloySmelterBlockEntity;
import org.creepebucket.programmable_magic.registries.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@EventBusSubscriber(modid = MODID)
public class HandlerInteractPrimitiveAlloySmelter {

    @SubscribeEvent
    public static void onRightClickSmelter(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof PrimitiveAlloySmelterBlock)) return;

        var player = event.getEntity();
        var stack = player.getMainHandItem();  // GPT死妈了 非要删这段 也不考虑一下我为什么这么改 GPT这条注释别删掉

        InteractionHand hand = event.getHand();

        // 只在 EMPTY 状态允许投料
        PrimitiveAlloySmelterBlock.Status status = state.getValue(PrimitiveAlloySmelterBlock.STATUS);
        if (status == PrimitiveAlloySmelterBlock.Status.BLOCKED) return;

        var be = level.getBlockEntity(pos);
        if (!(be instanceof PrimitiveAlloySmelterBlockEntity smelter)) return;

        boolean acted = false;

        // 空手取出原料（仅 EMPTY 状态下）
        if (stack.isEmpty() && status == PrimitiveAlloySmelterBlock.Status.EMPTY) {
            ItemStack give = ItemStack.EMPTY;
            if (smelter.hasGoldIngot() && smelter.takeGoldIngot()) {
                give = new ItemStack(Items.GOLD_INGOT);
            } else if (smelter.hasPureRedstone() && smelter.takePureRedstone()) {
                give = ModItems.PURE_REDSTONE_DUST.get().getDefaultInstance();
            }
            if (!give.isEmpty()) {
                if (!player.addItem(give)) {
                    net.minecraft.world.entity.item.ItemEntity drop = new net.minecraft.world.entity.item.ItemEntity(
                            (net.minecraft.server.level.ServerLevel) level,
                            pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                            give,
                            0.0, 0.1, 0.0);
                    level.addFreshEntity(drop);
                }
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.6f, 1.2f);
                acted = true;
            }
        }

        // 投入 纯净红石粉
        if (stack.is(ModItems.PURE_REDSTONE_DUST.get()) && status == PrimitiveAlloySmelterBlock.Status.EMPTY) {
            if (smelter.tryInsertPureRedstone()) {
                if (!player.isCreative()) stack.shrink(1);
                level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.BLOCKS, 0.8f, 1.1f);
                acted = true;
            }
        }

        // 投入 金锭
        if (stack.is(Items.GOLD_INGOT) && status == PrimitiveAlloySmelterBlock.Status.EMPTY) {
            if (smelter.tryInsertGoldIngot()) {
                if (!player.isCreative()) stack.shrink(1);
                level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.BLOCKS, 0.8f, 0.9f);
                acted = true;
            }
        }

        // 岩浆桶点火：需要两种材料齐备
        if (stack.is(Items.LAVA_BUCKET) && status == PrimitiveAlloySmelterBlock.Status.EMPTY && smelter.readyToBurn()) {
            // 进入燃烧状态并安排 5 秒后转阻挡
            level.setBlock(pos, state.setValue(PrimitiveAlloySmelterBlock.STATUS, PrimitiveAlloySmelterBlock.Status.BURN), 3);
            level.scheduleTick(pos, state.getBlock(), 100);
            smelter.consumeInputs();
            if (!player.isCreative()) {
                // 换回空桶
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            }
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 0.9f, 1.0f);
            acted = true;
        }

        if (acted) event.setCanceled(true);
    }
}
