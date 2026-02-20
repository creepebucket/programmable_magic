package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.gui.lib.api.hooks.Hook;
import org.creepebucket.programmable_magic.items.WandItemPlaceholder;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.creepebucket.programmable_magic.registries.ModItems;
import org.creepebucket.programmable_magic.registries.SpellRegistry;
import org.creepebucket.programmable_magic.spells.PackedSpell;
import org.creepebucket.programmable_magic.spells.SpellCompiler;
import org.creepebucket.programmable_magic.spells.api.SpellExceptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.minecraft.core.component.DataComponents.CUSTOM_NAME;

public class WandHooks {
    public static class SaveThemeHook extends Hook {
        public WandMenu menu;

        public SaveThemeHook(WandMenu menu) {
            super("save_theme");

            this.menu = menu;
        }

        @Override
        public void handle(Player player, Object... args) {
            var wand = player.getItemInHand(menu.hand);
            wand.set(ModDataComponents.THEME_MAIN_COLOR.get(), (Integer) args[0]);
            wand.set(ModDataComponents.THEME_BG_COLOR.get(), (Integer) args[1]);
            wand.set(ModDataComponents.THEME_TEXT_COLOR.get(), (Integer) args[2]);
        }
    }

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

            for (int i = storage.getContainerSize() - 1; i > index; i--)
                storage.setItem(i, storage.getItem(i - 1).copy());
            storage.setItem(index, ItemStack.EMPTY);
            player.containerMenu.broadcastChanges();
        }
    }

    public static class ImportSpellsHook extends Hook {
        private final Container storage;

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
        private final Container storage;

        public ClearSpellsHook(Container storage) {
            super("clear_spells");
            this.storage = storage;
        }

        @Override
        public void handle(Player player, Object... args) {
            for (int i = 0; i < storage.getContainerSize(); i++) storage.setItem(i, ItemStack.EMPTY);
        }
    }

    public static class PackSpellHook extends Hook {
        public Container packedSpellStorage;

        public PackSpellHook(Container packedSpellStorage) {
            super("pack_spell");
            this.packedSpellStorage = packedSpellStorage;
        }

        @Override
        public void handle(Player player, Object... args) {
            var storage = ((WandMenu) player.containerMenu).storedSpells;
            var name = (String) args[0];
            var desc = (String) args[1];
            var path = (String) args[2];

            var stack = new ItemStack(ModItems.PACKED_SPELL.get());
            stack.set(ModDataComponents.AUTHER, player.getGameProfile().name());
            stack.set(CUSTOM_NAME, Component.literal(name));
            stack.set(ModDataComponents.DESCRIPTION, desc);
            stack.set(ModDataComponents.RESOURCE_LOCATION, path);

            List<ItemStack> spells = new ArrayList<>(1024);
            for (ItemStack spell : storage) {
                if (spell.isEmpty()) continue;
                if (!SpellRegistry.isSpell(spell.getItem()) && !(spell.getItem() instanceof PackedSpell || spell.getItem() instanceof WandItemPlaceholder))
                    continue;

                spells.add(spell);
            }

            stack.set(ModDataComponents.SPELLS, spells);

            packedSpellStorage.setItem(0, stack);
        }
    }

    public static class PackedToStorageHook extends Hook {
        public Container spellStorage, packedStorage;

        public PackedToStorageHook(Container spellStorage, Container packedStorage) {
            super("packed_to_storage");
            this.spellStorage = spellStorage;
            this.packedStorage = packedStorage;
        }

        @Override
        public void handle(Player player, Object... args) {
            var count = 0;

            for (ItemStack spell : packedStorage.getItem(0).get(ModDataComponents.SPELLS)) {
                spellStorage.setItem(count, spell);
                count++;
            }
        }
    }

    public static class PackAndSupplyHook extends Hook {
        public Container customSupply;

        public PackAndSupplyHook(Container customSupply) {
            super("pack_and_supply");
            this.customSupply = customSupply;
        }

        @Override
        public void handle(Player player, Object... args) {
            var storage = ((WandMenu) player.containerMenu).storedSpells;
            var name = (String) args[0];
            var desc = (String) args[1];
            var path = (String) args[2];

            var stack = new ItemStack(ModItems.PACKED_SPELL.get());
            stack.set(ModDataComponents.AUTHER, player.getGameProfile().name());
            stack.set(CUSTOM_NAME, Component.literal(name));
            stack.set(ModDataComponents.DESCRIPTION, desc);
            stack.set(ModDataComponents.RESOURCE_LOCATION, path);

            List<ItemStack> spells = new ArrayList<>(1024);
            for (ItemStack spell : storage) {
                if (spell.isEmpty()) continue;
                if (!SpellRegistry.isSpell(spell.getItem()) && !(spell.getItem() instanceof PackedSpell || spell.getItem() instanceof WandItemPlaceholder))
                    continue;

                spells.add(spell);
            }

            stack.set(ModDataComponents.SPELLS, spells);

            // 寻找能放的位置
            for (int i = 0; i < 50; i++)
                if (customSupply.getItem(i).isEmpty()) {
                    customSupply.setItem(i, stack);
                    return;
                }
        }
    }

    public static class ReleaseSpellHook extends Hook {
        public WandMenu menu;

        public ReleaseSpellHook(WandMenu menu) {
            super("release_spell");

            this.menu = menu;
        }

        @Override
        public void handle(Player player, Object... args) {
            if (player.level().isClientSide()) return;

            var plugins = menu.wand.get(ModDataComponents.PLUGINS);

            // 编译
            var compiler = new SpellCompiler();
            var compiled = compiler.compile(menu.storedSpells, (Boolean) args[1]);

            if (!compiler.errors.isEmpty()) {
                for (SpellExceptions exception : compiler.errors) exception.throwIt(player);
                // 有错就滚
                return;
            }

            // 生成
            menu.spell = new SpellEntity(player.level(), player, compiled, new HashMap<>(), new ModUtils.Mana((Double) args[0], (Double) args[0], (Double) args[0], (Double) args[0]), plugins, menu.debugMode);

            player.level().addFreshEntity(menu.spell);
        }
    }

    public static class DebuggerStepHook extends Hook {
        public WandMenu menu;

        public DebuggerStepHook(WandMenu menu) {
            super("debugger_step");

            this.menu = menu;
        }

        @Override
        public void handle(Player player, Object... args) {
            if (menu.spell == null) return;
            menu.spell.doStep = true;
        }
    }

    public static class DebuggerTickHook extends Hook {
        public WandMenu menu;

        public DebuggerTickHook(WandMenu menu) {
            super("debugger_tick");

            this.menu = menu;
        }

        @Override
        public void handle(Player player, Object... args) {
            if (menu.spell == null) return;
            menu.spell.doTick = true;
        }
    }

    public static class DebuggerRunHook extends Hook {
        public WandMenu menu;

        public DebuggerRunHook(WandMenu menu) {
            super("debugger_run");

            this.menu = menu;
        }

        @Override
        public void handle(Player player, Object... args) {
            if (menu.spell == null) return;
            menu.spell.doRun = true;
        }
    }

    public static class DebuggerPauseHook extends Hook {
        public WandMenu menu;

        public DebuggerPauseHook(WandMenu menu) {
            super("debugger_pause");

            this.menu = menu;
        }

        @Override
        public void handle(Player player, Object... args) {
            if (menu.spell == null) return;
            menu.spell.doRun = false;
        }
    }

    public static class SetDebugModeHook extends Hook {
        public WandMenu menu;

        public SetDebugModeHook(WandMenu menu) {
            super("set_debug_mode");

            this.menu = menu;
        }

        @Override
        public void handle(Player player, Object... args) {
            menu.debugMode = (Boolean) args[0];
        }
    }

    public static class BreakConnectionHook extends Hook {
        public WandMenu menu;

        public BreakConnectionHook(WandMenu menu) {
            super("debugger_disconnect");

            this.menu = menu;
        }

        @Override
        public void handle(Player player, Object... args) {
            if (menu.spell == null) return;
            menu.spell.debugMode = false;
        }
    }
}
