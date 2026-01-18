package org.creepebucket.programmable_magic.spells;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.registries.SpellRegistry;
import org.creepebucket.programmable_magic.spells.api.SpellExceptions;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;
import org.creepebucket.programmable_magic.spells.spells_compute.CommaSpell;
import org.creepebucket.programmable_magic.spells.spells_compute.NumberDigitSpell;
import org.creepebucket.programmable_magic.spells.spells_compute.ParenSpell;
import org.creepebucket.programmable_magic.spells.spells_compute.ValueLiteralSpell;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.spells.SpellValueType.NUMBER;

public class SpellCompiler {
    public static SpellSequence compile(List<ItemStack> spells, Player caster) {

        // Step 1: List<ItemStack>转换为SpellSequence

        SpellSequence rawSequence = new SpellSequence();
        for (ItemStack spell : spells) {
            rawSequence.pushRight(SpellRegistry.createSpellLogic(spell.getItem()));
        }

        // Step 2: 编译前语法检查

        // 2.1: 检查括号等法术配对情况
        Map<Class<? extends SpellItemLogic.PairedLeftSpell>, Integer> pairsCount = new HashMap<>();

        for (SpellItemLogic spell = rawSequence.head; spell != null; spell = spell.next) {
            // 左括号为配对计数器+1
            if (spell instanceof SpellItemLogic.PairedLeftSpell)
                pairsCount.put((Class<SpellItemLogic.PairedLeftSpell>) spell.getClass(),
                        pairsCount.getOrDefault(spell.getClass(), 0) + 1);
            // 右括号为配对计数器-1
            if (spell instanceof SpellItemLogic.PairedRightSpell) {
                pairsCount.put(((SpellItemLogic.PairedRightSpell) spell).leftSpellType,
                        pairsCount.getOrDefault(((SpellItemLogic.PairedRightSpell) spell).leftSpellType, 0) - 1);
            }
        }

        // 如果配对计数器任意一项不为0, 则报错未配对法术
        for (Class<? extends SpellItemLogic.PairedLeftSpell> leftSpell : pairsCount.keySet()) {
            if (pairsCount.get(leftSpell) != 0) {
                try {
                    SpellExceptions.PAIRS_UNMATCHED(caster, leftSpell.getDeclaredConstructor().newInstance()).throwIt();
                    return new SpellSequence();
                } catch (InstantiationException e) { // 何意味
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // Step 3: 解析逗号和数字

        SpellItemLogic p = rawSequence.tail; // 可爱的指针
        while (p != null) {
            if (p instanceof NumberDigitSpell digit) {
                // 是数字就把值替换整段数字法术
                List<Object> result = digit.run(null, null, null, null).returnValue;
                SpellItemLogic next = ((NumberDigitSpell) result.get(1)).prev;
                rawSequence.replaceSection((NumberDigitSpell) result.get(1), p, new SpellSequence()
                        .pushRight(new ValueLiteralSpell(NUMBER, result.get(0)))); // 何意味
                p = next;
                continue;
            }

            if (p instanceof CommaSpell) { // 逗号就是空逻辑, 删掉就好
                SpellItemLogic next = p.prev;
                rawSequence.replaceSection(p, p, new SpellSequence());
                p = next;
                continue;
            }
            p = p.prev;
        }

        // Step 4: 配对括号等法术

        // 需要配对的法术栈
        Map<Class<? extends SpellItemLogic.PairedLeftSpell>, SpellSequence> pairs = new HashMap<>();
        for (SpellItemLogic i = rawSequence.head; i != null; i = i.next) {
            if (i instanceof SpellItemLogic.PairedLeftSpell) {
                // 左括号入栈
                pairs.computeIfAbsent((Class<? extends SpellItemLogic.PairedLeftSpell>) i.getClass(), k -> new SpellSequence())
                        .pushLeft(i);
            }
            if (i instanceof SpellItemLogic.PairedRightSpell) {
                // 右括号出栈并设置配对
                SpellItemLogic.PairedRightSpell r = (SpellItemLogic.PairedRightSpell) i;
                SpellItemLogic.PairedLeftSpell l = (SpellItemLogic.PairedLeftSpell) pairs.get(r.leftSpellType).popLeft();
                r.leftSpell = l;
                l.rightSpell = r;
            }
        }

        // Step 5: 解析法术序列

        // Shitting(?) Yard
        SpellSequence operatorStack = new SpellSequence();
        SpellSequence spellsRPN = new SpellSequence();

        // 遍历每个法术
        for (SpellItemLogic i = rawSequence.head; i != null; i = i.next) {

            // 左括号入栈
            if (i instanceof ParenSpell.LParenSpell) operatorStack.pushLeft(i);

                // 遇到右括号出栈运算符, 直到遇到左括号
            else if (i instanceof ParenSpell.RParenSpell) {
                // 遍历栈
                for (SpellItemLogic j = operatorStack.popLeft(); !(j instanceof ParenSpell.LParenSpell); j = operatorStack.popLeft())
                    spellsRPN.pushRight(j);
            }

            // 如果法术自己想要跳过需要尊重法术意愿
            else if (i.bypassShunting) spellsRPN.pushRight(i);

            else if (i instanceof ValueLiteralSpell) spellsRPN.pushRight(i);

            // 如果这个法术没有入参, 当作数值处理
            else if (i.inputTypes.get(0).get(0) == SpellValueType.EMPTY) { // 绝对不会NPE(?
                // 只检查第一个重载的第一个元素, 我们无法区分同时含有无参和有参的法术作为数字还是作为算符
                spellsRPN.pushRight(i);
            }

            // 剩下的就全是运算符
            // 1: i是左结合性 且 当i的优先级比栈顶高
            // 2: i是右结合性 且 i的优先级大于等于栈顶
            else if (operatorStack.head == null || (i.precedence > operatorStack.head.precedence) // i > op 总是入栈
                    || (i.precedence == operatorStack.head.precedence && i.rightConnectivity)) // 情况2
                operatorStack.pushLeft(i);

                // 否则出栈直到栈头的优先级小于i
            else {
                for (SpellItemLogic j = operatorStack.popLeft(); j.precedence <= i.precedence; j = operatorStack.popLeft()) {
                    spellsRPN.pushRight(j);

                    // 如果栈空跳出循环
                    if (operatorStack.head == null) break;
                }
            }
        }
        // 全部出栈
        if (operatorStack.head != null) // 操你妈的
            if (operatorStack.head != operatorStack.tail) // 何意味
                for (SpellItemLogic j = operatorStack.popLeft(); operatorStack.head != null; j = operatorStack.popLeft())
                    spellsRPN.pushRight(j);
            else spellsRPN.pushRight(operatorStack.popLeft()); // byd

        // Step 6: 编译后检查
        // TODO: 目前还没有

        return spellsRPN;
    }
}
