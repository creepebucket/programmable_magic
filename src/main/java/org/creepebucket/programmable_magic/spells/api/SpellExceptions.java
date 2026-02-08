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
    public SpellExceptions(String errorType, Component message, SpellItemLogic spell) {
        this.errorType = errorType;
        this.message = message;
        this.spell = spell;
    }

    public static SpellExceptions COMPILE(Component message, SpellItemLogic spell) {
        return new SpellExceptions(COMPILE, message, spell);
    }

    public static SpellExceptions RUNTIME(Component message, SpellItemLogic spell) {
        return new SpellExceptions(RUNTIME, message, spell);
    }

    // 需要配对的法术未配对
    public static SpellExceptions PAIRS_UNMATCHED(SpellItemLogic spell) {
        return SpellExceptions.COMPILE(Component.translatable("message.programmable_magic.error.pairs_unmatched"), spell);
    }

    // 无效输入
    public static SpellExceptions INVALID_INPUT(SpellItemLogic spell) {
        return SpellExceptions.RUNTIME(Component.translatable("message.programmable_magic.error.invalid_input"), spell);
    }

    // 编译错误

    // 魔力不足
    public static SpellExceptions NOT_ENOUGH_MANA(SpellItemLogic spell) {
        return SpellExceptions.RUNTIME(Component.translatable("message.programmable_magic.error.not_enough_mana"), spell);
    }

    // 运行时错误

    // 异常的本地化报错文本
    public Component message() {
        return Component.translatable(this.errorType).append(": ").append(
                Component.translatable("message.programmable_magic.error.detail.at_spell",
                        Component.translatable("item.programmable_magic.spell_display_" + spell.name), message));
    }

    // 实际抛出这个错误
    public void throwIt(Player player) {
        player.displayClientMessage(message, false);
    }
}
