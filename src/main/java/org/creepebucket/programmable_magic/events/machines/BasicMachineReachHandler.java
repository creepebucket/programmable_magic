package org.creepebucket.programmable_magic.events.machines;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@EventBusSubscriber(modid = MODID)
public class BasicMachineReachHandler {

    public static final Identifier basic_machine_reach = Identifier.fromNamespaceAndPath(MODID, "basic_machine_reach");
    public static final AttributeModifier reach_modifier = new AttributeModifier(basic_machine_reach, 5.0, AttributeModifier.Operation.ADD_VALUE);

    @SubscribeEvent
    public static void on_player_tick(PlayerTickEvent.Post event) {
        var player = event.getEntity();
        var attr = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (BasicMachineItemHelper.get_held_basic_machine(player) != null) attr.addOrUpdateTransientModifier(reach_modifier);
        else attr.removeModifier(basic_machine_reach);
    }
}

