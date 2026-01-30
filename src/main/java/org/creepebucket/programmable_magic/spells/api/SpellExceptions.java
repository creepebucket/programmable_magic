package org.creepebucket.programmable_magic.spells.api;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class SpellExceptions {
    public static final String COMPILE = "message.programmable_magic.error.types.compile";
    public static final String RUNTIME = "message.programmable_magic.error.types.runtime";
    public String errorType;
    public Component message;
    public Player player;
    public SpellItemLogic spell;

    // 构造函数
    public SpellExceptions(String errorType, Component message, Player player, SpellItemLogic spell) {
        this.errorType = errorType;
        this.message = message;
        this.player = player;
        this.spell = spell;
    }

    public static SpellExceptions COMPILE(Component message, Player player, SpellItemLogic spell) {
        return new SpellExceptions(COMPILE, message, player, spell);
    }

    public static SpellExceptions RUNTIME(Component message, Player player, SpellItemLogic spell) {
        return new SpellExceptions(RUNTIME, message, player, spell);
    }

    // 需要配对的法术未配对
    public static SpellExceptions PAIRS_UNMATCHED(Player player, SpellItemLogic spell) {
        return SpellExceptions.COMPILE(Component.translatable("message.programmable_magic.error.pairs_unmatched"), player, spell);
    }

    // 无效输入
    public static SpellExceptions INVALID_INPUT(Player player, SpellItemLogic spell) {
        return SpellExceptions.RUNTIME(Component.translatable("message.programmable_magic.error.invalid_input"), player, spell);
    }

    // 编译错误

    // 魔力不足
    public static SpellExceptions NOT_ENOUGH_MANA(Player player, SpellItemLogic spell) {
        return SpellExceptions.RUNTIME(Component.translatable("message.programmable_magic.error.not_enough_mana"), player, spell);
    }

    // 运行时错误

    // 异常的本地化报错文本
    public Component message() {
        return Component.translatable(this.errorType).append(": ").append(message).append(
                Component.translatable("message.programmable_magic.error.detail.at_spell")).append(spell.name);
    }

    // 实际抛出这个错误
    public void throwIt() {
        player.displayClientMessage(message, false);
    }
}
