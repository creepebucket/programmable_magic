package org.creepebucket.programmable_magic.spells.spells_compute;

import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.api.ExecutionResult;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public abstract class StorageSpell extends SpellItemLogic implements SpellItemLogic.ComputeMod{

    public StorageSpell() {
        subCategory = "spell." + MODID + ".subcategory.data_storage";
    }

    @Override
    public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return true;
    }

    @Override
    public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return new ModUtils.Mana();
    }

    public static class SetStoreSpell extends StorageSpell {
        public SetStoreSpell() {
            super();
            name = "set_store";
            inputTypes = List.of(List.of(SpellValueType.NUMBER, SpellValueType.ANY));
            precedence = -99;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            int index = (int) Math.floor((Double) paramsList.get(0));

            spellEntity.spellData.put(getStoreKey(index), paramsList.get(1));

            return ExecutionResult.SUCCESS(this);
        }
    }

    public static class GetStoreSpell extends StorageSpell {
        public GetStoreSpell() {
            super();
            name = "get_store";
            inputTypes = List.of(List.of(SpellValueType.NUMBER));
            outputTypes = List.of(List.of(SpellValueType.ANY));
            precedence = 5;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            int index = (int) Math.floor((Double) paramsList.get(0));

            Object value = spellEntity.spellData.get(getStoreKey(index));
            return ExecutionResult.RETURNED(this, List.of(value), List.of(SpellValueType.fromValue(value)));
        }
    }

    public static String getStoreKey(int index) {

        // 拼接存储key
        return "data_storage_" + index;
    }
}
