package org.creepebucket.programmable_magic.gui.lib.api;

import java.util.function.Function;

public class SmoothedValue {
    public double current, speed = 0, target, last;
    public boolean bound = false;

    public SmoothedValue(double initial) {
        current = initial;
        target = initial;
    }

    public SmoothedValue set(double value) {
        target = value;
        return this;
    }

    public void setImmediate(double value) {
        target = value;
        current = value;
    }

    public double get() {
        return current;
    }

    public int getInt() {
        return (int) get();
    }

    public void doStep(double dt) {
        last = current;
        speed += (target - current) * 200 * dt - speed * (Math.abs(target - current) < 2 ? 10 : 30) * dt; // 末端缓冲
        current += speed * dt;

        if ((target - current) * (target - last) < 0) {
            speed = 0;
            current = target;
        }
    }

    public TransformedValue add(double n) {
        return new TransformedValue((b) -> b + n, this);
    }

    public TransformedValue minus(double n) {
        return new TransformedValue((b) -> b - n, this);
    }

    public TransformedValue multiply(double n) {
        return new TransformedValue((b) -> b * n, this);
    }

    public TransformedValue divide(double n) {
        return new TransformedValue((b) -> b / n, this);
    }

    public static class TransformedValue extends SmoothedValue {
        // 原smoothed 变换后的只读值
        public Function<Double, Double> transform;
        public SmoothedValue parent;

        public TransformedValue(Function<Double, Double> transform, SmoothedValue parent) {
            super(0);
            this.transform = transform;
            this.parent = parent;
        }

        @Override
        public SmoothedValue set(double d) {
            throw new RuntimeException("不能设置TransformedValue");
        }

        @Override
        public void setImmediate(double d) {
            throw new RuntimeException("不能设置TransformedValue");
        }

        @Override
        public void doStep(double d) {
            throw new RuntimeException("不能设置TransformedValue");
        }

        @Override
        public double get() {
            return transform.apply(parent.get());
        }

        @Override
        public int getInt() {
            return (int) get();
        }
    }
}
