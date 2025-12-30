package org.creepebucket.programmable_magic.mananet.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.creepebucket.programmable_magic.ModUtils.Mana;

import java.util.UUID;

public interface MananetNode {

    Mana getCache();

    void setCache(Mana mana);

    Mana getLoad();

    void setLoad(Mana mana);

    void addMana(Mana mana);

    boolean getConnectivity(Direction direction);

    void setConnectivity(Direction direction, boolean connectivity);

    default void setConnectivity(boolean connectivity, Direction direction) {
        setConnectivity(direction, connectivity);
    }

    Mana getMana();

    default Mana getmana() {
        return getMana();
    }

    boolean canProduce();

    UUID getNetworkId();

    void setNetworkId(UUID networkId);

    Level getNodeLevel();

    BlockPos getNodePos();

    String getNodeRegistryId();
}

