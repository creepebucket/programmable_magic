package org.creepebucket.programmable_magic.spells.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.spells_compute.ValueLiteralSpell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;
import static org.creepebucket.programmable_magic.registries.WandPluginRegistry.getPlugin;

public abstract class SpellItemLogic implements Cloneable {

    // 链表成员指针
    public SpellItemLogic next;
    public SpellItemLogic prev;

    // 运算属性
    public int precedence = 0;
    public boolean rightConnectivity = false;

    // 是否跳过编译的后缀转换
    public boolean bypassShunting = false;

    // 出入参
    public List<List<SpellValueType>> inputTypes = List.of(List.of(SpellValueType.EMPTY));
    public List<List<SpellValueType>> outputTypes = List.of(List.of(SpellValueType.EMPTY));

    // 注册名
    public String name;

    // 法术子类别名
    public String subCategory = "spell." + MODID + ".subcategory.none";

    /*
     * 法术实体触发的每tick钩子
     * 供类似"弹丸附加"使用 每刻判断伤害实体碰撞并结算伤害
     */
    public static void taggedTick(SpellEntity spellEntity) {
    }

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
    public ExecutionResult runWithCheck(Player caster, SpellSequence spellSequence, SpellEntity spellEntity) {

        // 根据自身重载长度截取指定长度的参数
        List<List<Object>> allParamsList = new ArrayList<>();
        Map<Integer, Boolean> haveStored = new java.util.HashMap<>();

        for (List<SpellValueType> overload : inputTypes) {
            // 检查这个长度是否已经获取过
            if (haveStored.getOrDefault(overload.size(), false)) continue;

            SpellItemLogic p = this; // 追踪参数
            List<Object> paramsList = new ArrayList<>();

            // 获取参数
            for (int i = 0; i < overload.size(); i++) {
                // 先判断null
                if (p == null) break;

                // 加入参数列表
                paramsList.add(p);
                p = p.next;
            }

            haveStored.put(overload.size(), true);
            allParamsList.add(paramsList);
        }

        // 检查参数是否在重载里
        boolean matched = false;
        boolean partMatched = true;
        List<Object> matchedParamsList = null;

        for (List<Object> paramsList : allParamsList) {
            // 对于每个参数列表

            for (List<SpellValueType> overload : inputTypes) {
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
                if (partMatched) matchedParamsList = paramsList;

                matched = matched || partMatched;
            }
        }

        // 如果未匹配，则返回错误
        if (!matched) {
            SpellExceptions.INVALID_INPUT(caster, this).throwIt();
            return ExecutionResult.ERRORED();
        }

        List<Object> paramsList = matchedParamsList;

        // 再检查是否能运行
        if (!canRun(caster, spellSequence, paramsList, spellEntity)) return ExecutionResult.ERRORED();

        // 最后检查魔力是否足够
        if (getManaCost(caster, spellSequence, paramsList, spellEntity).anyGreaterThan(spellEntity.availableMana)) {
            SpellExceptions.NOT_ENOUGH_MANA(caster, this).throwIt();
            return ExecutionResult.ERRORED();
        }

        // 保证可以运行再运行

        // 插件的运行前逻辑
        for (ItemStack plugin : spellEntity.pluginItems)
            getPlugin(plugin.getItem()).beforeSpellExecution(spellEntity, this, spellEntity.spellData, spellSequence, paramsList);

        ExecutionResult result = run(caster, spellSequence, paramsList, spellEntity);

        // 插件的运行后逻辑
        for (ItemStack plugin : spellEntity.pluginItems)
            getPlugin(plugin.getItem()).afterSpellExecution(spellEntity, this, spellEntity.spellData, spellSequence, paramsList);

        // 扣魔力
        spellEntity.availableMana.subtract(getManaCost(caster, spellSequence, paramsList, spellEntity));

        if (result.returnValue == null) return result;

        // 使用返回值构造序列
        SpellSequence returns = new SpellSequence();
        for (Object o : result.returnValue) returns.pushRight(new ValueLiteralSpell(SpellValueType.fromValue(o), o));

        // 替换原法术
        // 找到L
        SpellItemLogic L = this;
        for (Object p : paramsList) L = L.prev;
        // 替换
        spellSequence.replaceSection(L, this, returns);

        return result;
    }

    // 一些内部具体法术类/接口

    // 4种法术类型
    public interface BaseSpell {
    }

    public interface ComputeMod {
    }

    public interface ControlMod {
    }

    public interface AdjustMod {
    }

    // 左右可配对法术
    public abstract static class PairedLeftSpell extends SpellItemLogic {

        public PairedRightSpell rightSpell;
        public Class<? extends PairedRightSpell> rightSpellType;
    }

    public abstract static class PairedRightSpell extends SpellItemLogic {

        public PairedLeftSpell leftSpell;
        public Class<? extends PairedLeftSpell> leftSpellType;
    }
}
