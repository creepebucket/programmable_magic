package org.creepebucket.programmable_magic.spells.api;

import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;

public class ExecutionResult {
    // 下个执行的法术指针
    public SpellItemLogic nextSpell;
    // 在执行完后, 延迟多少刻
    public int delayTicks;
    // 执行完后, 应该停止执行吗
    public boolean doStop;
    // 返回值
    public List<Object> returnValue;
    // 返回值类型
    public List<SpellValueType> returnTypes;

    // 构造函数
    public ExecutionResult(SpellItemLogic nextSpell, int delayTicks, boolean doStop, List<Object> returnValue, List<SpellValueType> returnTypes) {
        this.nextSpell = nextSpell;
        this.delayTicks = delayTicks;
        this.doStop = doStop;
        this.returnValue = returnValue;
        this.returnTypes = returnTypes;
    }

    /*
     * 法术执行成功.
     * 将指针设为下一个法术, 不延时
     */
    public static ExecutionResult SUCCESS(SpellItemLogic spell) {
        return new ExecutionResult(spell.next, 0, false, null, null);
    }

    /*
     * 法术执行失败.
     * 在下一刻重试当前法术
     */
    public static ExecutionResult FAILED(SpellItemLogic spell) {
        return new ExecutionResult(spell, 1, false, null, null);
    }

    /*
     * 法术运行时报错.
     * 停止执行, 并设置报错信息
     */
    public static ExecutionResult ERRORED() {
        return new ExecutionResult(null, 0, true, null, null);
    }

    /*
     * 法术执行完毕.
     * 停止执行
     */
    public static ExecutionResult COMPLETED() {
        return new ExecutionResult(null, 0, true, null, null);
    }

    /*
     * 法术执行完毕.
     * 返回值
     */
    public static ExecutionResult RETURNED(SpellItemLogic spell, List<Object> returnValue, List<SpellValueType> returnTypes) {
        return new ExecutionResult(spell.next, 0, false, returnValue, returnTypes);
    }
}
