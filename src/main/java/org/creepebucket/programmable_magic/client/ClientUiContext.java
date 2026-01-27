package org.creepebucket.programmable_magic.client;

import net.minecraft.client.gui.Font;

public class ClientUiContext {
    private static Font font;

    public static Font getFont() { return font; }

    public static void setFont(Font font) { ClientUiContext.font = font; }
}

