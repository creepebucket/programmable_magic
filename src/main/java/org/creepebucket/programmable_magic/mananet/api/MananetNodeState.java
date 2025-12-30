package org.creepebucket.programmable_magic.mananet.api;

import org.creepebucket.programmable_magic.ModUtils.Mana;

import java.util.UUID;

public class MananetNodeState {

    public UUID networkId;
    public Mana cache = new Mana();
    public Mana load = new Mana();
    public int connectivityMask = 0b111111;
}

