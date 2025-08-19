package org.creepebucket.programmable_magic.data;

import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.creepebucket.programmable_magic.registries.ModBlockTagProvider;
import org.creepebucket.programmable_magic.registries.ModItemTagProvider;

public class ModDataGenerators {
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(ModBlockTagProvider::new);
        event.createProvider(output -> new ModItemTagProvider(output, event.getLookupProvider()));
    }
} 