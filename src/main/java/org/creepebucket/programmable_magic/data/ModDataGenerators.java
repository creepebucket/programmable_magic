package org.creepebucket.programmable_magic.data;

import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.creepebucket.programmable_magic.registries.ModBlockTagProvider;
import org.creepebucket.programmable_magic.registries.ModItemTagProvider;
import org.creepebucket.programmable_magic.data.SpellItemModelProvider;

public class ModDataGenerators {
    // 使用 Client 子类（与 run config 的 clientData 匹配），保持原先可工作的行为
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(ModBlockTagProvider::new);
        event.createProvider(output -> new ModItemTagProvider(output, event.getLookupProvider()));
        // 生成法术的模型 + client items（1.21+ 必需）
        event.createProvider(SpellItemModelProvider::new);
    }
}
