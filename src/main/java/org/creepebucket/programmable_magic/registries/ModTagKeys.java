package org.creepebucket.programmable_magic.registries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModTagKeys {
    public static final TagKey<Item> SPELL_BASE_EFFECT = ItemTags.create(ResourceLocation.fromNamespaceAndPath(MODID, "spell/base"));
    public static final TagKey<Item> SPELL_ADJUST_MOD = ItemTags.create(ResourceLocation.fromNamespaceAndPath(MODID, "spell/modifier/adjust"));
    public static final TagKey<Item> SPELL_CONTROL_MOD = ItemTags.create(ResourceLocation.fromNamespaceAndPath(MODID, "spell/modifier/control"));
    public static final TagKey<Item> SPELL_COMPUTE_MOD = ItemTags.create(ResourceLocation.fromNamespaceAndPath(MODID, "spell/modifier/compute"));
    public static final TagKey<Item> SPELL_TARGET_MOD = ItemTags.create(ResourceLocation.fromNamespaceAndPath(MODID, "spell/modifier/target"));

    public static final TagKey<Item> SPELL = ItemTags.create(ResourceLocation.fromNamespaceAndPath(MODID, "spell"));
    public static final TagKey<Item> SPELL_MOD = ItemTags.create(ResourceLocation.fromNamespaceAndPath(MODID, "spell/modifier"));
}
