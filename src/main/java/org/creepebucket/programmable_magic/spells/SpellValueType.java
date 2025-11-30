package org.creepebucket.programmable_magic.spells;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum SpellValueType {
    ENTITY("Entity", ChatFormatting.DARK_AQUA),
    NUMBER("Number", ChatFormatting.GOLD),
    VECTOR3("Vector3", ChatFormatting.AQUA),
    MODIFIER("Modifier", ChatFormatting.LIGHT_PURPLE),
    SPELL("Spell", ChatFormatting.GREEN),
    ANY("Any", ChatFormatting.GRAY),
    ITEM("Item", ChatFormatting.RED),
    EMPTY("", ChatFormatting.GRAY);

    private final String display;
    private final ChatFormatting color;

    SpellValueType(String display, ChatFormatting color) {
        this.display = display;
        this.color = color;
    }

    public Component typed() {
        return Component.literal(display).withStyle(color);
    }

    public ChatFormatting color() { return color; }
    public String display() { return display; }
}

