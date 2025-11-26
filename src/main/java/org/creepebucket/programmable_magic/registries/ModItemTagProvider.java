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
        boolean hasAdjust = false, hasControl = false, hasCompute = false;

        // 始终创建基础法术标签（即使为空），避免被上层引用时缺失
        this.tag(ModTagKeys.SPELL_BASE_EFFECT);

        // 自动为法术添加标签并记录是否存在对应类别
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
                    hasAdjust = true;
                    break;
                case CONTROL_MOD:
                    tag(ModTagKeys.SPELL_CONTROL_MOD).add(itemSupplier.get());
                    hasControl = true;
                    break;
                case COMPUTE_MOD:
                    tag(ModTagKeys.SPELL_COMPUTE_MOD).add(itemSupplier.get());
                    hasCompute = true;
                    break;
                default:
                    break;
            }
        }

        // 若存在对应分类，则确保这些分类标签被创建（即使没有实际条目）
        if (hasAdjust) this.tag(ModTagKeys.SPELL_ADJUST_MOD);
        if (hasCompute) this.tag(ModTagKeys.SPELL_COMPUTE_MOD);
        if (hasControl) this.tag(ModTagKeys.SPELL_CONTROL_MOD);

        var spellModTag = this.tag(ModTagKeys.SPELL_MOD);
        if (hasAdjust) spellModTag.addTag(ModTagKeys.SPELL_ADJUST_MOD);
        if (hasCompute) spellModTag.addTag(ModTagKeys.SPELL_COMPUTE_MOD);
        if (hasControl) spellModTag.addTag(ModTagKeys.SPELL_CONTROL_MOD);

        var spellRoot = this.tag(ModTagKeys.SPELL)
                .addTag(ModTagKeys.SPELL_BASE_EFFECT);
        if (hasAdjust || hasCompute || hasControl) {
            spellRoot.addTag(ModTagKeys.SPELL_MOD);
        }
    }
}
