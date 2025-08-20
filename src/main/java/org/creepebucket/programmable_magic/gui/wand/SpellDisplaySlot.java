package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/*
法术展示物品槽位
不能被拿取
 */
public class SpellDisplaySlot extends SlotItemHandler {
    private final BaseWamdMenu menu;
    private final ItemStack spell;

    public SpellDisplaySlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, BaseWamdMenu menu, ItemStack spell) {
        super(itemHandler, index, xPosition, yPosition);

        this.menu = menu;
        this.spell = spell.copy();

        itemHandler.insertItem(index, this.spell.copy(), false);
    }

    // 绝对不许拿走! >_<

    @Override
    public boolean mayPlace(ItemStack stack) {return false;}

    @Override
    public boolean mayPickup(Player playerIn) {return false;}

    @Override
    public ItemStack remove(int amount) {return ItemStack.EMPTY;}

    @Override
    public void onTake(Player player, ItemStack stack) {}

    @Override
    public void setByPlayer(ItemStack stack) {}

    @Override
    public void setByPlayer(ItemStack newStack, ItemStack oldStack) {}

    @Override
    public void set(ItemStack stack) {}

    @Override
    public ItemStack safeInsert(ItemStack stack) {return stack;}

    @Override
    public ItemStack safeInsert(ItemStack stack, int increment) {return stack;}

    @Override
    public boolean allowModification(Player player) {return false;}

    // 看你怎么拿走! awa

    public void handleClick() {
        menu.sendSpellToStorage(this.getItem().copy());
    }

    public void resetToDefault() {
        this.getItemHandler().extractItem(this.getSlotIndex(), this.getItem().getCount(), false);
        this.getItemHandler().insertItem(this.getSlotIndex(), this.spell.copy(), false);
    }

}
