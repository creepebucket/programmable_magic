package org.creepebucket.programmable_magic.spells.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.ArrayList;
import java.util.List;

public abstract class SpellItemLogic implements Cloneable {

    // 链表成员指针
    public SpellItemLogic next;
    public SpellItemLogic prev;

    // 运算属性
    public int precedence = 0;
    public boolean rightConnectivity = false;
    public int rightParamOffset = 0;

    // 是否跳过编译的后缀转换
    public boolean bypassShunting = false;

    // 出入参
    public List<List<SpellValueType>> inputTypes;
    public List<List<SpellValueType>> outputTypes;

    // 注册名
    public String name;

    @Override
    public SpellItemLogic clone() {
        try {
            SpellItemLogic clone = (SpellItemLogic) super.clone();
            clone.next = null;
            clone.prev = null;
            clone.inputTypes = new ArrayList<>();
            for (List<SpellValueType> overload : inputTypes) clone.inputTypes.add(new ArrayList<>(overload));
            clone.outputTypes = new ArrayList<>();
            for (List<SpellValueType> overload : outputTypes) clone.outputTypes.add(new ArrayList<>(overload));
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /*
     * 执行一个法术
     * 注意: 保证参数合法, 否则请使用 runWithCheck
     * @param caster 法术施法者
     * @param spellSequence 法术序列
     * @param paramsList 法术参数列表
     * @return 法术执行结果
     */
    public abstract ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity);

    /*
     * 是否能运行的检查
     * @return 是否能运行
     */
    public abstract boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity);

    /*
     * 魔力消耗计算
     */
    public abstract ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity);

    /*
     * 带检验的法术执行
     * 通常情况下, 请调用我而不是 run
     */
    public ExecutionResult runWithCheck(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        // 检查参数是否在重载里
        boolean matched = false;
        boolean partMatched = true;

        for(List<SpellValueType> overload: inputTypes) {
            // 先标记部分匹配再检查是否有违反
            partMatched = true;

            // 先检查长度
            if (overload.size() != paramsList.size()) {
                partMatched = false;
                continue;
            }

            // 再进行逐类型检查
            for (int i = 0; i < overload.size(); i++) {
                // 检查类型
                SpellValueType type = overload.get(i);
                if (type != SpellValueType.ANY && type != SpellValueType.fromValue(paramsList.get(i))) {
                    partMatched = false;
                    break;
                }
            }

            // 最后整合匹配信息
            matched = matched || partMatched;
        }

        // 如果未匹配，则返回错误
        if (!matched) {
            SpellExceptions.INVALID_INPUT(caster, this).throwIt();
            return ExecutionResult.ERRORED();
        }

        // 再检查是否能运行
        if (!canRun(caster, spellSequence, paramsList, spellEntity)) return ExecutionResult.ERRORED();

        // 最后检查魔力是否足够
        if (spellEntity.availableMana <= getManaCost(caster, spellSequence, paramsList, spellEntity)) {
            SpellExceptions.NOT_ENOUGH_MANA(caster, this).throwIt();
            return ExecutionResult.ERRORED();
        }

        // 保证可以运行再运行
        return run(caster, spellSequence, paramsList, spellEntity);
    }

    // 一些内部具体法术类/接口

    // 左右可配对法术
    public abstract static class PairedLeftSpell extends SpellItemLogic {

        public PairedRightSpell rightSpell;
        public Class<? extends PairedRightSpell> rightSpellType;
    }

    public abstract static class PairedRightSpell extends SpellItemLogic {

        public PairedLeftSpell leftSpell;
        public Class<? extends PairedLeftSpell> leftSpellType;
    }

    // 4种法术类型
    public interface BaseSpell {}
    public interface ComputeMod {}
    public interface ControlMod {}
    public interface AdjustMod {}
}
