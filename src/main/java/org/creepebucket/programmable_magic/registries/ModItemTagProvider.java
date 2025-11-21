package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import java.util.concurrent.CompletableFuture;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, MODID);
    }

    // 兼容 NeoForge 的 createBlockAndItemTags：提供带 Block 标签 Provider 的重载构造
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, TagsProvider<Block> blockTagsProvider) {
        this(output, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 自动为法术添加标签
        for (var entry : SpellRegistry.getRegisteredSpells().entrySet()) {
            var itemSupplier = entry.getKey();
            var logicSupplier = entry.getValue();
            var logic = logicSupplier.get();

            switch (logic.getSpellType()) {
                case BASE_SPELL:
                    tag(ModTagKeys.SPELL_BASE_EFFECT).add(itemSupplier.get());
                    break;
                case ADJUST_MOD:
                    tag(ModTagKeys.SPELL_ADJUST_MOD).add(itemSupplier.get());
                    break;
                case CONTROL_MOD:
                    tag(ModTagKeys.SPELL_CONTROL_MOD).add(itemSupplier.get());
                    break;
                case COMPUTE_MOD:
                    tag(ModTagKeys.SPELL_COMPUTE_MOD).add(itemSupplier.get());
                    break;
                case TARGET_MOD:
                    tag(ModTagKeys.SPELL_TARGET_MOD).add(itemSupplier.get());
                    break;
            }
        }

        this.tag(ModTagKeys.SPELL_MOD)
                .addTag(ModTagKeys.SPELL_ADJUST_MOD)
                .addTag(ModTagKeys.SPELL_COMPUTE_MOD)
                .addTag(ModTagKeys.SPELL_CONTROL_MOD)
                .addTag(ModTagKeys.SPELL_TARGET_MOD);

        this.tag(ModTagKeys.SPELL)
                .addTag(ModTagKeys.SPELL_MOD)
                .addTag(ModTagKeys.SPELL_BASE_EFFECT);
    }
}
