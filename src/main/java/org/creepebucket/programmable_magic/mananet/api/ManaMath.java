package org.creepebucket.programmable_magic.mananet.api;

import org.creepebucket.programmable_magic.ModUtils.Mana;

public final class ManaMath {

    private ManaMath() {}

    public static Mana copy(Mana mana) {
        return new Mana(mana.getRadiation(), mana.getTemperature(), mana.getMomentum(), mana.getPressure());
    }

    public static Mana delta(Mana next, Mana prev) {
        return new Mana(
                next.getRadiation() - prev.getRadiation(),
                next.getTemperature() - prev.getTemperature(),
                next.getMomentum() - prev.getMomentum(),
                next.getPressure() - prev.getPressure()
        );
    }

    public static Mana scale(Mana mana, double factor) {
        return new Mana(
                mana.getRadiation() * factor,
                mana.getTemperature() * factor,
                mana.getMomentum() * factor,
                mana.getPressure() * factor
        );
    }

    public static Mana clampNonNegative(Mana mana) {
        return new Mana(
                Math.max(0.0, mana.getRadiation()),
                Math.max(0.0, mana.getTemperature()),
                Math.max(0.0, mana.getMomentum()),
                Math.max(0.0, mana.getPressure())
        );
    }

    public static Mana clampToCache(Mana mana, Mana cache) {
        return new Mana(
                Math.min(mana.getRadiation(), cache.getRadiation()),
                Math.min(mana.getTemperature(), cache.getTemperature()),
                Math.min(mana.getMomentum(), cache.getMomentum()),
                Math.min(mana.getPressure(), cache.getPressure())
        );
    }

    public static boolean canAfford(Mana mana, Mana cost) {
        return mana.getRadiation() >= cost.getRadiation()
                && mana.getTemperature() >= cost.getTemperature()
                && mana.getMomentum() >= cost.getMomentum()
                && mana.getPressure() >= cost.getPressure();
    }

    public static Mana positivePart(Mana mana) {
        return new Mana(
                Math.max(0.0, mana.getRadiation()),
                Math.max(0.0, mana.getTemperature()),
                Math.max(0.0, mana.getMomentum()),
                Math.max(0.0, mana.getPressure())
        );
    }

    public static double cacheWeight(Mana cache) {
        return cache.getRadiation() + cache.getTemperature() + cache.getMomentum() + cache.getPressure();
    }
}
