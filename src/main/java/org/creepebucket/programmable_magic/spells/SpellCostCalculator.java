package org.creepebucket.programmable_magic.spells;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.registries.SpellRegistry;
import org.creepebucket.programmable_magic.spells.compute_mod.ComputeRuntime;

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

            // 构造当前组的工作副本 [i..baseIndex]，避免在计算中修改原序列
            List<SpellItemLogic> working = new ArrayList<>(spellSequence.subList(i, baseIndex + 1));
            // 为成本计算做一次“运行期等价折叠”：只执行 compute_mod，按其提供的 __seq_replace 折叠序列
            working = normalizeComputeForCost(working, groupData, player);
            // 基础法术是工作副本的最后一个元素
            SpellItemLogic baseLogic = working.get(working.size() - 1);

            // 计算基础耗魔：把工作副本作为序列传给计算逻辑
            try {
                groupData.setCustomData("__seq", working);
                groupData.setCustomData("__idx", working.size() - 1);
                baseLogic.calculateBaseMana(groupData);
            } finally {
                groupData.clearCustomData("__idx");
            }

            // 从右到左应用修正：对工作副本中 base 左侧的所有法术调用 applyManaModification
            for (int k = working.size() - 2; k >= 0; k--) {
                SpellItemLogic mod = working.get(k);
                groupData.setCustomData("__idx", k);
                mod.applyManaModification(groupData);
            }

			for (String manaType : List.of("radiation", "temperature", "momentum", "pressure")) {
				double add = groupData.getManaCost(manaType);
				if (add != 0) totals.put(manaType, totals.get(manaType) + add);
			}

			i = baseIndex + 1;
		}

		return totals;
	}

    // 将工作序列中的 compute_mod 节点按运行期策略就地规约，得到稳定布局（例如 [ENTITY, VECTOR3, BASE]）。
    private static List<SpellItemLogic> normalizeComputeForCost(List<SpellItemLogic> original, SpellData data, Player player) {
        if (original == null || original.isEmpty()) return original;
        // 工作副本，允许被替换
        List<SpellItemLogic> work = new ArrayList<>(original);
        int safety = 0;
        boolean changed;
        do {
            changed = false;
            // 逐个尝试执行 compute_mod，以驱动括号/运算符/向量构造等自我折叠
            for (int idx = 0; idx < work.size(); idx++) {
                SpellItemLogic node = work.get(idx);
                if (node == null || node.getSpellType() != SpellItemLogic.SpellType.COMPUTE_MOD) continue;
                data.setCustomData("__seq", work);
                data.setCustomData("__idx", idx);
                node.run(player, data);
                // 若该节点给出整体替换，采纳之并清空缓存
                Object rep = data.getCustomData("__seq_replace", Object.class);
                Integer toIdx = data.getCustomData("__idx_replace", Integer.class);
                if (rep instanceof List<?> newList) {
                    //noinspection unchecked
                    work = new ArrayList<>((List<SpellItemLogic>) newList);
                    data.clearComputeCache();
                    data.clearCustomData("__seq_replace");
                    data.clearCustomData("__idx_replace");
                    // 重启一次扫描；toIdx 作为起点有助于快速收敛
                    if (toIdx != null) idx = Math.max(-1, toIdx - 1);
                    changed = true;
                }
            }
            safety++;
        } while (changed && safety < 64);

        data.clearCustomData("__idx");
        return work;
    }
}
