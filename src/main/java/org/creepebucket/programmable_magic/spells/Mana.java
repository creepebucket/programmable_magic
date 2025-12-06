package org.creepebucket.programmable_magic.spells;

import java.util.HashMap;
import java.util.Map;

public final class Mana {

    public static final String RADIATION = "radiation";
    public static final String TEMPERATURE = "temperature";
    public static final String MOMENTUM = "momentum";
    public static final String PRESSURE = "pressure";

    private final Map<String, Double> values;

    public Mana(Double radiation, Double temperature, Double momentum, Double pressure) {
        this.values = new HashMap<>();
        // 预置四系键，初始为 0.0
        values.put(RADIATION, radiation);
        values.put(TEMPERATURE, temperature);
        values.put(MOMENTUM, momentum);
        values.put(PRESSURE, pressure);
    }

    public Mana() {
        this(0.0, 0.0, 0.0, 0.0);
    }

    public Map<String, Double> toMap() {
        return values;
    }

    public void add(String key, Double value) {
        values.put(key, values.get(key) + value);
    }

    public void add(Mana mana) {
        for (Map.Entry<String, Double> entry : mana.toMap().entrySet()) {
            values.put(entry.getKey(), values.get(entry.getKey()) + entry.getValue());
        }
    }

    public Double getRadiation() {
        return values.get(RADIATION);
    }
    public Double getTemperature() {
        return values.get(TEMPERATURE);
    }
    public Double getMomentum() {
        return values.get(MOMENTUM);
    }
    public Double getPressure() {
        return values.get(PRESSURE);
    }

    public Mana negative() {
        Mana mana = new Mana();
        for (Map.Entry<String, Double> entry : values.entrySet()) {
            mana.add(entry.getKey(), -entry.getValue());
        }
        return mana;
    }

    public boolean greaterThan(Mana mana) {
        return values.get(RADIATION) > mana.getRadiation() &&
                values.get(TEMPERATURE) > mana.getTemperature() &&
                values.get(MOMENTUM) > mana.getMomentum() &&
                values.get(PRESSURE) > mana.getPressure();
    }

    public boolean lessThan(Mana mana) {
        return values.get(RADIATION) < mana.getRadiation() &&
                values.get(TEMPERATURE) < mana.getTemperature() &&
                values.get(MOMENTUM) < mana.getMomentum() &&
                values.get(PRESSURE) < mana.getPressure();
    }

    // 任一分量大于即返回真：用于“是否有任一系魔力不足”的判定
    public boolean anyGreaterThan(Mana mana) {
        return values.get(RADIATION) > mana.getRadiation() ||
                values.get(TEMPERATURE) > mana.getTemperature() ||
                values.get(MOMENTUM) > mana.getMomentum() ||
                values.get(PRESSURE) > mana.getPressure();
    }
}
