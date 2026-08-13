package org.creepebucket.arcanism.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.resources.Identifier;
import org.creepebucket.arcanism.gui.wand.WandScreen;
import org.jetbrains.annotations.NotNull;

import static org.creepebucket.arcanism.Arcanism.MODID;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    @Override
    public @NotNull Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(MODID, "jei_plugin");
    }

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        registration.addGuiScreenHandler(WandScreen.class, screen -> null);
    }
}
