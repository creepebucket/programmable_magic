package org.creepebucket.programmable_magic.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.resources.ResourceLocation;
import org.creepebucket.programmable_magic.gui.wand.WandScreen;
import org.jetbrains.annotations.NotNull;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MODID, "jei_plugin");
    }

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        registration.addGuiScreenHandler(WandScreen.class, screen -> null);
    }
}
