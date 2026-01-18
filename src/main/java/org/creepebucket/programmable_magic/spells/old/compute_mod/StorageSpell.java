package org.creepebucket.programmable_magic.spells.old.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.old.SpellData;
import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.old.SpellSequence;
import org.creepebucket.programmable_magic.spells.old.SpellUtils;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.ModUtils.formatSpellError;
import static org.creepebucket.programmable_magic.spells.old.SpellUtils.setSpellError;
import static org.creepebucket.programmable_magic.spells.SpellValueType.*;

public class StorageSpell {

    private static Map<Double, Object> ensureStore(SpellData data) {
        Map<Double, Object> store = data.getCustomData("compute_storage", Map.class);
        if (store == null) {
            store = new HashMap<>();
            data.setCustomData("compute_storage", store);
        }
        return store;
    }

    private static SpellValueType deduceType(Object v) {
        return SpellValueType.fromValue(v);
    }

    public static class StoreInputSpell extends BaseComputeModLogic {
        @Override
        public String getRegistryName() { return "compute_set_storage"; }

        @Override
        public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.storage"); }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            Object value = spellParams.get(0);
            Double index = (Double) spellParams.get(1);
            Map<Double, Object> store = ensureStore(data);
            store.put(index, value);
            return Map.of("successful", true);
        }

        

        @Override
        public List<Component> getTooltip() {
            return List.of(
                    Component.translatable("tooltip.programmable_magic.spell.set_storage.desc1"),
                    Component.translatable("tooltip.programmable_magic.spell.set_storage.desc2")
            );
        }

        @Override
        public List<List<SpellValueType>> getNeededParamsType() {
            return List.of(List.of(ANY, NUMBER));
        }

        @Override
        public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(EMPTY)); }
    }

    public static class StoreOutputSpell extends BaseComputeModLogic {
        @Override
        public String getRegistryName() { return "compute_get_storage"; }

        @Override
        public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.storage"); }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            Double index = (Double) spellParams.get(0);
            Map<Double, Object> store = ensureStore(data);
            Object v = store.get(index);
            if (v == null) {
                int spellIndex = SpellUtils.displayIndexOf(spellSequence, this);
                setSpellError(player, data, formatSpellError(
                        Component.translatable("message.programmable_magic.error.kind.param"),
                        Component.translatable("message.programmable_magic.error.detail.storage_empty", index)
                                .append(Component.literal(" "))
                                .append(Component.translatable("message.programmable_magic.error.detail.at_spell", spellIndex, getRegistryName()))
                ));
                return Map.of("successful", false, "should_discard", true);
            }
            return Map.of("successful", true, "type", deduceType(v), "value", v);
        }

        

        @Override
        public List<Component> getTooltip() {
            return List.of(
                    Component.translatable("tooltip.programmable_magic.spell.get_storage.desc1"),
                    Component.translatable("tooltip.programmable_magic.spell.get_storage.desc2")
            );
        }

        @Override
        public List<List<SpellValueType>> getNeededParamsType() { return List.of(List.of(NUMBER)); }

        @Override
        public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(ANY)); }
    }
}
