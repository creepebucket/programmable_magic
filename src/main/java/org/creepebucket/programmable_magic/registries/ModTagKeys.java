package org.creepebucket.programmable_magic.registries;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModTagKeys {
    public static final TagKey<Item> SPELL_BASE_EFFECT = ItemTags.create(Identifier.fromNamespaceAndPath(MODID, "spell/base"));
    public static final TagKey<Item> SPELL_ADJUST_MOD = ItemTags.create(Identifier.fromNamespaceAndPath(MODID, "spell/modifier/adjust"));
    public static final TagKey<Item> SPELL_CONTROL_MOD = ItemTags.create(Identifier.fromNamespaceAndPath(MODID, "spell/modifier/control"));
    public static final TagKey<Item> SPELL_COMPUTE_MOD = ItemTags.create(Identifier.fromNamespaceAndPath(MODID, "spell/modifier/compute"));

    public static final TagKey<Item> SPELL = ItemTags.create(Identifier.fromNamespaceAndPath(MODID, "spell"));
    public static final TagKey<Item> SPELL_MOD = ItemTags.create(Identifier.fromNamespaceAndPath(MODID, "spell/modifier"));

    // 金属粒标签：用于“弹丸附加”法术的参数校验
    public static final TagKey<Item> METAL_PELLET = ItemTags.create(Identifier.fromNamespaceAndPath(MODID, "pellet/metal"));
}
