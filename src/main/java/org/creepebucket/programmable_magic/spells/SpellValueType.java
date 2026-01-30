package org.creepebucket.programmable_magic.spells;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public enum SpellValueType {
    ENTITY("entity", "Entity", ChatFormatting.DARK_BLUE, Entity.class),
    NUMBER("number", "Number", ChatFormatting.GOLD, Double.class),
    VECTOR3("vector3", "Vector3", ChatFormatting.AQUA, Vec3.class),
    STRING("string", "String", ChatFormatting.YELLOW, String.class),
    BLOCK("block", "Block", ChatFormatting.DARK_GREEN, BlockState.class),
    MODIFIER("modifier", "Modifier", ChatFormatting.LIGHT_PURPLE, null),
    SPELL("spell", "Spell", ChatFormatting.GREEN, null),
    ANY("any", "Any", ChatFormatting.GRAY, null),
    ITEM("item", "Item", ChatFormatting.RED, ItemStack.class),
    BOOLEAN("boolean", "Boolean", ChatFormatting.BLUE, Boolean.class),
    EMPTY("empty", "", ChatFormatting.GRAY, null);

    private final String id;
    private final String display;
    private final ChatFormatting color;
    private final Class<?> javaClass;

    SpellValueType(String id, String display, ChatFormatting color, Class<?> javaClass) {
        this.id = id;
        this.display = display;
        this.color = color;
        this.javaClass = javaClass;
    }

    public static SpellValueType fromValue(Object value) {
        for (SpellValueType t : values()) {
            if (t.matches(value)) return t;
        }
        return ANY;
    }

    public static Component allTyped() {
        var out = Component.empty();
        boolean first = true;
        for (SpellValueType t : values()) {
            if (t == EMPTY) continue;
            if (!first) out.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
            out.append(t.typed());
            first = false;
        }
        return out;
    }

    public Component typed() {
        return Component.literal(display).withStyle(color);
    }

    public String id() {
        return id;
    }

    public boolean matches(Object value) {
        return javaClass != null && javaClass.isInstance(value);
    }

    public ChatFormatting color() {
        return color;
    }

    public String display() {
        return display;
    }
}
