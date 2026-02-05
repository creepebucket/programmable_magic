package org.creepebucket.programmable_magic.gui.lib.api.hooks;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class HookManager {
    private final Map<String, Hook> hooks = new HashMap<>();
    private BiConsumer<String, Object[]> sendToServer;

    public <T extends Hook> T hook(T hook) {
        hooks.put(hook.id(), hook);
        hook.bind(this);
        return hook;
    }

    public void bindServerSender(BiConsumer<String, Object[]> sender) {
        this.sendToServer = sender;
    }

    void trigger(String id, Object[] args) {
        this.sendToServer.accept(id, args);
    }

    public void handleOnServer(String id, Player player, Object[] args) {
        hooks.get(id).handle(player, args);
    }
}

