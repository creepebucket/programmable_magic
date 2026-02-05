package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.gui.lib.api.hooks.Hook;

public class WandHooks {
    public static class StoredSpellsEditHook extends Hook {
        private final Container storage;

        public StoredSpellsEditHook(Container storage) {
            super("stored_spells_edit");
            this.storage = storage;
        }

        @Override
        public void handle(Player player, Object... args) {
            int index = (Integer) args[0];
            boolean deleteBlank = (Boolean) args[1];
            if (index < 0) index = 0;

            if (deleteBlank) {
                for (int i = index; i < storage.getContainerSize() - 1; i++) {
                    if (i < 0) continue;
                    storage.setItem(i, storage.getItem(i + 1).copy());
                }
                storage.setItem(storage.getContainerSize() - 1, ItemStack.EMPTY);
                player.containerMenu.broadcastChanges();
                return;
            }

            for (int i = storage.getContainerSize() - 1; i > index; i--) storage.setItem(i, storage.getItem(i - 1).copy());
            storage.setItem(index, ItemStack.EMPTY);
            player.containerMenu.broadcastChanges();
        }
    }

    public static class ImportSpellsHook extends Hook {
        private Container storage;

        public ImportSpellsHook(Container storage) {
            super("import_spells");
            this.storage = storage;
        }

        @Override
        public void handle(Player player, Object... args) {
            String string = (String) args[0];
            Container imported = ModUtils.deSerializeSpells(string);
            for (int i = 0; i < storage.getContainerSize(); i++) storage.setItem(i, imported.getItem(i).copy());
        }
    }

    public static class ClearSpellsHook extends Hook {
        private Container storage;

        public ClearSpellsHook(Container storage) {
            super("clear_spells");
            this.storage = storage;
        }

        @Override
        public void handle(Player player, Object... args) {
            for (int i = 0; i < storage.getContainerSize(); i++) storage.setItem(i, ItemStack.EMPTY);
        }
    }
}
