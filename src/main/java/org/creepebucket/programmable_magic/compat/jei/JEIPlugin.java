package org.creepebucket.programmable_magic.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.resources.Identifier;
import org.creepebucket.programmable_magic.gui.lib.ui.UiScreenBase;
import org.jetbrains.annotations.NotNull;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    @Override
    public @NotNull Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(MODID, "jei_plugin");
    }

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        registration.addGuiScreenHandler(UiScreenBase.class, screen -> null);
    }
}
