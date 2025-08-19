package org.creepebucket.programmable_magic;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ModUtils {
    public static List<ItemStack> getItemsFromTag(TagKey<Item> tagKey) {
        List<ItemStack> items = new ArrayList<>();

        BuiltInRegistries.ITEM.stream().filter(item -> {try {
            return item.getDefaultInstance().is(tagKey);
        } catch (Exception e) {return false;}
        }).forEach(item -> {try{
            items.add(new ItemStack(item));} catch (Exception e) {}
        });

        return items;
    }
}
