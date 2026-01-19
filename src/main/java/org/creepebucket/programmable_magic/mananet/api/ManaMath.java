package org.creepebucket.programmable_magic.mananet.api;

import org.creepebucket.programmable_magic.ModUtils.Mana;

/**
 * {@link Mana} 的轻量数学工具。
 *
 * <p>这里的所有运算都返回新对象，避免在调用点隐式修改入参。</p>
 */
public final class ManaMath {

    private ManaMath() {}

    /**
     * 复制一份 Mana。
     */
    public static Mana copy(Mana mana) {
        return new Mana(mana.getRadiation(), mana.getTemperature(), mana.getMomentum(), mana.getPressure());
    }

    /**
     * 计算差值：{@code next - prev}。
     */
    public static Mana delta(Mana next, Mana prev) {
        return new Mana(
                next.getRadiation() - prev.getRadiation(),
                next.getTemperature() - prev.getTemperature(),
                next.getMomentum() - prev.getMomentum(),
                next.getPressure() - prev.getPressure()
        );
    }

    /**
     * 缩放：{@code availableMana * factor}。
     */
    public static Mana scale(Mana mana, double factor) {
        return new Mana(
                mana.getRadiation() * factor,
                mana.getTemperature() * factor,
                mana.getMomentum() * factor,
                mana.getPressure() * factor
        );
    }

    /**
     * 将每个分量夹到非负：{@code max(0, x)}。
     */
    public static Mana clampNonNegative(Mana mana) {
        return new Mana(
                Math.max(0.0, mana.getRadiation()),
                Math.max(0.0, mana.getTemperature()),
                Math.max(0.0, mana.getMomentum()),
                Math.max(0.0, mana.getPressure())
        );
    }

    /**
     * 将每个分量夹到缓存上限：{@code min(availableMana, cache)}。
     */
    public static Mana clampToCache(Mana mana, Mana cache) {
        return new Mana(
                Math.min(mana.getRadiation(), cache.getRadiation()),
                Math.min(mana.getTemperature(), cache.getTemperature()),
                Math.min(mana.getMomentum(), cache.getMomentum()),
                Math.min(mana.getPressure(), cache.getPressure())
        );
    }

    /**
     * 是否能支付 cost（逐分量比较）。
     */
    public static boolean canAfford(Mana mana, Mana cost) {
        return mana.getRadiation() >= cost.getRadiation()
                && mana.getTemperature() >= cost.getTemperature()
                && mana.getMomentum() >= cost.getMomentum()
                && mana.getPressure() >= cost.getPressure();
    }

    /**
     * 取正部：{@code max(0, x)}。
     *
     * <p>常用于把“每秒净负载”拆成“需要支付的消耗部分”（正数）与“可免费获得的产出部分”（负数）。</p>
     */
    public static Mana positivePart(Mana mana) {
        return new Mana(
                Math.max(0.0, mana.getRadiation()),
                Math.max(0.0, mana.getTemperature()),
                Math.max(0.0, mana.getMomentum()),
                Math.max(0.0, mana.getPressure())
        );
    }

    /**
     * 计算缓存权重（四个分量求和）。
     *
     * <p>用于某些“按容量占比分配”的场景时，提供一个简单的标量。</p>
     */
    public static double cacheWeight(Mana cache) {
        return cache.getRadiation() + cache.getTemperature() + cache.getMomentum() + cache.getPressure();
    }
}
