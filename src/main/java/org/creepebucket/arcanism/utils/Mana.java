package org.creepebucket.arcanism.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.Map;

public final class Mana {

    public static final String RADIATION = "radiation";
    public static final String TEMPERATURE = "temperature";
    public static final String MOMENTUM = "momentum";
    public static final String PRESSURE = "pressure";
    public static final Codec<Mana> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("radiation").forGetter(Mana::getRadiation),
                    Codec.DOUBLE.fieldOf("temperature").forGetter(Mana::getTemperature),
                    Codec.DOUBLE.fieldOf("momentum").forGetter(Mana::getMomentum),
                    Codec.DOUBLE.fieldOf("pressure").forGetter(Mana::getPressure)
            ).apply(instance, Mana::new)
    );
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

    public Mana add(Mana mana) {
        return new Mana(
                values.get(RADIATION) + mana.getRadiation(),
                values.get(TEMPERATURE) + mana.getTemperature(),
                values.get(MOMENTUM) + mana.getMomentum(),
                values.get(PRESSURE) + mana.getPressure()
        );
    }

    public Mana subtract(Mana mana) {
        return new Mana(
                values.get(RADIATION) - mana.getRadiation(),
                values.get(TEMPERATURE) - mana.getTemperature(),
                values.get(MOMENTUM) - mana.getMomentum(),
                values.get(PRESSURE) - mana.getPressure()
        );
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

    public Mana scale(double factor) {
        return new Mana(
                values.get(RADIATION) * factor,
                values.get(TEMPERATURE) * factor,
                values.get(MOMENTUM) * factor,
                values.get(PRESSURE) * factor
        );
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

    public Mana min(Mana mana) {
        return new Mana(
                Math.min(getRadiation(), mana.getRadiation()),
                Math.min(getTemperature(), mana.getTemperature()),
                Math.min(getMomentum(), mana.getMomentum()),
                Math.min(getPressure(), mana.getPressure())
        );
    }

    /**
     * 任一分量大于即返回真：用于“是否有任一系魔力不足”的判定
     */
    public boolean anyGreaterThan(Mana mana) {
        return values.get(RADIATION) > mana.getRadiation() ||
                values.get(TEMPERATURE) > mana.getTemperature() ||
                values.get(MOMENTUM) > mana.getMomentum() ||
                values.get(PRESSURE) > mana.getPressure();
    }
}
