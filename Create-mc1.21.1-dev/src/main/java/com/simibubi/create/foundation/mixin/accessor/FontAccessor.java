package com.simibubi.create.foundation.mixin.accessor;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;

@Mixin(Font.class)
public interface FontAccessor {
	@Accessor("fonts")
	Function<ResourceLocation, FontSet> create$getFonts();
}
