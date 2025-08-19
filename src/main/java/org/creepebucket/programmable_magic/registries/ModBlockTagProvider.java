package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 目前为空，如果需要添加块标签，可以在这里添加
    }
}
