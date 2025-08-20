package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;
import org.creepebucket.programmable_magic.registries.ModTagKeys;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static org.creepebucket.programmable_magic.ModUtils.getItemsFromTag;
import static org.creepebucket.programmable_magic.registries.ModTagKeys.SPELL;

public class WandSpellStorageMenu extends BaseWamdMenu {
    public WandSpellStorageMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL, extraData.readInt(), extraData.readDouble());
    }

    public WandSpellStorageMenu(int containerId, Inventory playerInventory, ContainerLevelAccess levelAccess, int slots, double manaMult) {
        super(ModMenuTypes.WAND_SPELL_STORAGE_MENU.get(), containerId, playerInventory, levelAccess, slots - 5, manaMult, ModDataComponents.WAND_SPELLS_STORAGE.value());
    }
}
