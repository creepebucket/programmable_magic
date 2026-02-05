package org.creepebucket.programmable_magic.gui.lib.api.hooks;

import net.minecraft.world.entity.player.Player;

public abstract class Hook {
    private final String id;
    private HookManager manager;

    public Hook(String id) {
        this.id = id;
    }

    public final String id() {
        return this.id;
    }

    final void bind(HookManager manager) {
        this.manager = manager;
    }

    public final void trigger(Object... args) {
        this.manager.trigger(this.id, args);
    }

    public abstract void handle(Player player, Object... args);
}

