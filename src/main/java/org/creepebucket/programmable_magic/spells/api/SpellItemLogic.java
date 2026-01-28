package org.creepebucket.programmable_magic.spells.api;

import net.minecraft.advancements.criterion.ShotCrossbowTrigger;
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
    public int rightParamOffset = 0;

    // 是否跳过编译的后缀转换
    public boolean bypassShunting = false;

    // 出入参
    public List<List<SpellValueType>> inputTypes = List.of(List.of(SpellValueType.EMPTY));
    public List<List<SpellValueType>> outputTypes = List.of(List.of(SpellValueType.EMPTY));

    // 注册名
    public String name;

    // 法术子类别名
    public String subCategory = "spell." + MODID + ".subcategory.none";

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
     * 法术实体触发的每tick钩子
     * 供类似"弹丸附加"使用 每刻判断伤害实体碰撞并结算伤害
     */
    public void taggedTick(SpellEntity spellEntity) {}

    /*
     * 带检验的法术执行
     * 通常情况下, 请调用我而不是 run
     */
    public ExecutionResult runWithCheck(Player caster, SpellSequence spellSequence, SpellEntity spellEntity) {

        // 对法术列表进行预处理, 获取法术前面的参数和类型
        List<SpellValueType> paramsTypes = new ArrayList<>();
        List<Object> paramsList = new ArrayList<>();

        // 法术参数追踪的指针
        SpellItemLogic p = this.prev;

        while (p instanceof ValueLiteralSpell v) { // 获取此法术前的所有 ValueLiteral 序列
            paramsTypes.addFirst(v.type);
            paramsList.addFirst(v.value);
            p = p.prev;
        }

        boolean matched = false;

        for (List<SpellValueType> overload : inputTypes) {

            if (overload.get(0) == SpellValueType.EMPTY) { // 傻逼空入参判断
                matched = true;
                paramsList = List.of();
                break;
            }

            if (paramsTypes.size() < overload.size()) continue; // 先比较长度避免截取失败

            // 长度合法就可以开始截取子列表了
            var subTypes = new ArrayList<>(paramsTypes.subList(paramsTypes.size() - overload.size(), paramsTypes.size()));

            // 把所有的ANY替换掉, 避免匹配这些内容
            for (int i = 0; i < overload.size(); i++) if (overload.get(i) == SpellValueType.ANY) subTypes.set(i, SpellValueType.ANY);

            if (overload.equals(subTypes)) { // 现在就可以放心检测了
                // 如果匹配, 截断paramsList
                paramsList = new ArrayList<>(paramsList.subList(paramsTypes.size() - overload.size(), paramsTypes.size()));
                matched = true;
                // 跳出循环, 避免截取后再截取一遍出错
                break;
            }
        }

        if (!matched) {
            SpellExceptions.INVALID_INPUT(caster, this).throwIt();
            return ExecutionResult.ERRORED();
        }

        // 再检查是否能运行
        if (!canRun(caster, spellSequence, paramsList, spellEntity)) return ExecutionResult.ERRORED();

        // 最后检查魔力是否足够
        if (getManaCost(caster, spellSequence, paramsList, spellEntity).anyGreaterThan(spellEntity.availableMana)) {
            SpellExceptions.NOT_ENOUGH_MANA(caster, this).throwIt();
            return ExecutionResult.ERRORED();
        }

        // 保证可以运行再运行

        // 插件的运行前逻辑
        for (ItemStack plugin : spellEntity.pluginItems) getPlugin(plugin.getItem()).beforeSpellExecution(spellEntity, this, spellEntity.spellData, spellSequence, paramsList);

        ExecutionResult result = run(caster, spellSequence, paramsList, spellEntity);

        // 插件的运行后逻辑
        for (ItemStack plugin : spellEntity.pluginItems) getPlugin(plugin.getItem()).afterSpellExecution(spellEntity, this, spellEntity.spellData, spellSequence, paramsList);

        // 扣魔力
        spellEntity.availableMana.subtract(getManaCost(caster, spellSequence, paramsList, spellEntity));

        if (result.returnValue == null) return result;

        // 使用返回值构造序列
        SpellSequence returns = new SpellSequence();
        for(Object o : result.returnValue) returns.pushRight(new ValueLiteralSpell(SpellValueType.fromValue(o), o));

        // 替换原法术
        // 找到L
        SpellItemLogic L = this;
        for (Object __p: paramsList) L = L.prev;
        // 替换
        spellSequence.replaceSection(L, this, returns);

        return result;
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
