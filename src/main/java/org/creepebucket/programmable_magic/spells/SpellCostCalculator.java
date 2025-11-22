package org.creepebucket.programmable_magic.spells;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.registries.SpellRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure utility for computing required mana for a sequence of spells.
     * Groups like M...MB (M = modifier, B = base) with base being BASE_SPELL,
 * computes group cost from right to left, and sums groups.
 */
public final class SpellCostCalculator {
	private SpellCostCalculator() {}

	public static Map<String, Double> computeRequiredManaFromStacks(List<ItemStack> spellStacks, Player player) {
		List<SpellItemLogic> sequence = new ArrayList<>();
		for (ItemStack stack : spellStacks) {
			Item item = stack.getItem();
			SpellItemLogic logic = SpellRegistry.createSpellLogic(item);
			if (logic != null) sequence.add(logic);
		}
		return computeRequiredManaFromLogics(sequence, player);
	}

	public static Map<String, Double> computeRequiredManaFromLogics(List<SpellItemLogic> spellSequence, Player player) {
		Map<String, Double> totals = new HashMap<>();
		totals.put("radiation", 0.0);
		totals.put("temperature", 0.0);
		totals.put("momentum", 0.0);
		totals.put("pressure", 0.0);

		int i = 0;
		while (i < spellSequence.size()) {
			int baseIndex = -1;
            for (int j = i; j < spellSequence.size(); j++) {
                SpellItemLogic.SpellType type = spellSequence.get(j).getSpellType();
                if (type == SpellItemLogic.SpellType.BASE_SPELL) {
                    baseIndex = j;
                    break;
                }
            }
			if (baseIndex == -1) break; // trailing modifiers without base are ignored as a group delimiter

			Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);
			Vec3 look = player.getLookAngle();
			SpellData groupData = new SpellData(player, playerPos, look);

            SpellItemLogic baseLogic = spellSequence.get(baseIndex);
            if (baseLogic.getSpellType() == SpellItemLogic.SpellType.BASE_SPELL) {
                baseLogic.calculateBaseMana(groupData);
            }
            // Only BASE_SPELL acts as boundary; modifiers can still transform zeros

            // 预计算 compute_mod 表达式结果：从左到右模拟运行 compute 片段
            try {
                groupData.setCustomData("__seq", spellSequence);
                for (int j = i; j < baseIndex; j++) {
                    SpellItemLogic sj = spellSequence.get(j);
                    if (sj.getSpellType() == SpellItemLogic.SpellType.COMPUTE_MOD) {
                        groupData.setCustomData("__idx", j);
                        sj.run(player, groupData);
                    }
                }
            } catch (Exception ignored) {}

            for (int k = baseIndex - 1; k >= i; k--) {
                SpellItemLogic mod = spellSequence.get(k);
                if (mod.getSpellType() != SpellItemLogic.SpellType.BASE_SPELL) {
                    mod.applyManaModification(groupData);
                }
			}

			for (String manaType : List.of("radiation", "temperature", "momentum", "pressure")) {
				double add = groupData.getManaCost(manaType);
				if (add != 0) totals.put(manaType, totals.get(manaType) + add);
			}

			i = baseIndex + 1;
		}

		return totals;
	}
} 
