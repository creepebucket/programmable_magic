package org.creepebucket.programmable_magic.gui.lib.api;

import static org.creepebucket.programmable_magic.ModUtils.now;

public abstract class Animation {
    public double start, duration = 0; // 秒
    public double dx = 0, dy = 0, dw = 0, dh = 0;
    public double alphaMultMain = 1, alphaMultBg = 1, alphaMultText = 1;

    public boolean isActive() {
        var now = now();
        return now >= start;
    }

    public boolean isExpired() {
        return start + duration <= now();
    }

    public abstract void step(double dt);

    public void onExpire(Widget widget) {
    }

    public static class FadeIn extends Animation {
        public FadeIn(double duration) {
            this.duration = duration;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public void step(double dt) {
            var mult = (now() - start) / duration;
            alphaMultMain = mult;
            alphaMultBg = mult;
            alphaMultText = mult;
        }

        public static class FromLeft extends FadeIn {
            public FromLeft(double duration) {
                super(duration);
            }

            @Override
            public void step(double dt) {
                super.step(dt);
                dx = -Math.pow(start + duration - now(), 3) * 1000;
            }
        }

        public static class FromRight extends FadeIn {
            public FromRight(double duration) {
                super(duration);
            }

            @Override
            public void step(double dt) {
                super.step(dt);
                dx = Math.pow(start + duration - now(), 3) * 1000;
            }
        }

        public static class FromTop extends FadeIn {
            public FromTop(double duration) {
                super(duration);
            }

            @Override
            public void step(double dt) {
                super.step(dt);
                dy = -Math.pow(start + duration - now(), 3) * 1000;
            }
        }

        public static class FromBottom extends FadeIn {
            public FromBottom(double duration) {
                super(duration);
            }

            @Override
            public void step(double dt) {
                super.step(dt);
                dy = Math.pow(start + duration - now(), 3) * 1000;
            }
        }
    }

    public static class FadeOut extends Animation {
        public FadeOut(double duration) {
            this.duration = duration;
        }

        @Override
        public void step(double dt) {
            var mult = 1 - (now() - start) / duration;
            alphaMultMain = mult;
            alphaMultBg = mult;
            alphaMultText = mult;
        }

        @Override
        public void onExpire(Widget widget) {
            widget.removeMyself();
        }

        public static class ToLeft extends FadeOut {
            public ToLeft(double duration) {
                super(duration);
            }

            @Override
            public void step(double dt) {
                super.step(dt);
                dx = -Math.pow(now() - start, 3) * 1000;
            }
        }

        public static class ToRight extends FadeOut {
            public ToRight(double duration) {
                super(duration);
            }

            @Override
            public void step(double dt) {
                super.step(dt);
                dx = Math.pow(now() - start, 3) * 1000;
            }
        }

        public static class ToTop extends FadeOut {
            public ToTop(double duration) {
                super(duration);
            }

            @Override
            public void step(double dt) {
                super.step(dt);
                dy = -Math.pow(now() - start, 3) * 1000;
            }
        }

        public static class ToBottom extends FadeOut {
            public ToBottom(double duration) {
                super(duration);
            }

            @Override
            public void step(double dt) {
                super.step(dt);
                dy = Math.pow(now() - start, 3) * 1000;
            }
        }
    }

    public static class Transparent extends Animation {
        public Transparent(double duration) {
            this.duration = duration;
        }

        @Override
        public void step(double dt) {
            alphaMultMain = 0;
            alphaMultBg = 0;
            alphaMultText = 0;
        }
    }
}
