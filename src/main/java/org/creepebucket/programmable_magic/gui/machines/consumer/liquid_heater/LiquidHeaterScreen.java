package org.creepebucket.programmable_magic.gui.machines.consumer.liquid_heater;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.lib.api.Animation;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.widgets.FluidTextureWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.NumberDisplayWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.TextWidget;
import org.creepebucket.programmable_magic.gui.machines.api.MachineScreen;
import org.creepebucket.programmable_magic.gui.machines.api.MachineWidgets;
import org.creepebucket.programmable_magic.utils.ModColors;

public class LiquidHeaterScreen extends MachineScreen<LiquidHeaterMenu> {
	public FluidTextureWidget fluidTextureInput, fluidTextureOutput;
	public TextWidget fluidNameTextInput, fluidIdTextInput, fluidNameTextOutput, fluidIdTextOutput;

	public LiquidHeaterScreen(LiquidHeaterMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}

	@Override
	public void buildWidget() {
		super.buildWidget();

		root.mainColor(ModColors.MAIN_COLOR_T);

		var productionWindow = addWidget(new MachineWidgets.InformationWindowWidget(Coordinate.fromCenter(-200, -100), Coordinate.fromTopLeft(400, 100), Component.literal("生产总览"), 0, 0));

		// 头
		productionWindow.addChild(new TextWidget(Coordinate.fromTopLeft(7, 39), Component.literal("机器类型")).noShadow().bottomAlignY().mainColor(0xff7f7f7f));
		productionWindow.addChild(new TextWidget(Coordinate.fromTopLeft(7, 15), Component.literal("液体加热器")).scaled(1.5));
		productionWindow.addChild(new TextWidget(Coordinate.fromTopRight(-7, 39), Component.literal("魔力类型")).noShadow().rightAlign().bottomAlignY().mainColor(0xff7f7f7f));
		productionWindow.addChild(new TextWidget(Coordinate.fromTopRight(-7, 15), Component.literal("温度/Tem")).scaled(1.5).rightAlign());

		// 转换能量
		productionWindow.addChild(new TextWidget(Coordinate.fromCenterBottom(0, -5), Component.literal(">>>")).noShadow().scaled(3).bottomAlignY().centerAlign().mainColor(0x1fffffff));
		var conversionCostUnit = productionWindow.addChild(new TextWidget(Coordinate.fromCenterBottom(8, -25), Component.literal("J/L")).noShadow().mainColor(0xbfffffff));
		conversionCostUnit.addChild(new NumberDisplayWidget(Coordinate.fromTopLeft(-1, -1), menu.conversionCost, 6, 1, true).mainColor(0xbfffffff).rightAlign());

		// 输入
		productionWindow.addChild(new NumberDisplayWidget(Coordinate.fromCenterBottom(-40, -30), menu.inputSpeed, 6, 2, true).rightAlign());
		productionWindow.addChild(new TextWidget(Coordinate.fromCenterBottom(-37, -18), Component.literal("L/min")).rightAlign().mainColor(-1));

		// fluidTextureInput = (FluidTextureWidget) productionWindow.addChild(new FluidTextureWidget(Coordinate.fromCenterBottom(-75, -33), Coordinate.fromTopLeft(10, 10), menu.inputId.get()).rightAlign());
		fluidNameTextInput = (TextWidget) productionWindow.addChild(new TextWidget(Coordinate.fromCenterBottom(-110, -32), menu.inputId.get().isEmpty() ? Component.literal("") :
				Component.translatable(BuiltInRegistries.FLUID.getValue(Identifier.parse(menu.inputId.get())).getFluidType().getDescriptionId())).mainColor(-1));
		fluidIdTextInput = (TextWidget) productionWindow.addChild(new TextWidget(Coordinate.fromCenterBottom(-107, -32), Component.literal(menu.inputId.get())).scaled(0.5).noShadow().mainColor(0x7f7f7f7f));

		menu.inputId.whenDataChangedDo(() -> {
			//fluidTextureInput.addAnimation(new Animation.FadeOut.ToRight(0.3), 0);
			fluidNameTextInput.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.05);
			fluidIdTextInput.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.1);

			//fluidTextureInput = (FluidTextureWidget) productionWindow.addChild(new FluidTextureWidget(Coordinate.fromTopLeft(0, 12), Coordinate.fromTopLeft(10, 10), menu.inputId.get()).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0));
			fluidNameTextInput = (TextWidget) productionWindow.addChild(new TextWidget(Coordinate.fromCenterBottom(-110, -32), menu.inputId.get().isEmpty() ? Component.literal("") :
					Component.translatable(BuiltInRegistries.FLUID.getValue(Identifier.parse(menu.inputId.get())).getFluidType().getDescriptionId())).mainColor(-1).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.05));
			fluidIdTextInput = (TextWidget) productionWindow.addChild(new TextWidget(Coordinate.fromCenterBottom(-107, -32), Component.literal(menu.inputId.get())).scaled(0.5).noShadow().mainColor(0x7f7f7f7f).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.1));
		});

		// 输出
		productionWindow.addChild(new NumberDisplayWidget(Coordinate.fromCenterBottom(105, -30), menu.outputSpeed, 6, 2, true).rightAlign());
		productionWindow.addChild(new TextWidget(Coordinate.fromCenterBottom(108, -18), Component.literal("L/min")).rightAlign().mainColor(-1));

		// fluidTextureOutput = (FluidTextureWidget) productionWindow.addChild(new FluidTextureWidget(Coordinate.fromCenterBottom(-75, -33), Coordinate.fromTopLeft(10, 10), menu.outputId.get()).rightAlign());
		fluidNameTextOutput = (TextWidget) productionWindow.addChild(new TextWidget(Coordinate.fromCenterBottom(35, -32), menu.outputId.get().isEmpty() ? Component.literal("") :
				Component.translatable(BuiltInRegistries.FLUID.getValue(Identifier.parse(menu.outputId.get())).getFluidType().getDescriptionId())).mainColor(-1));
		fluidIdTextOutput = (TextWidget) productionWindow.addChild(new TextWidget(Coordinate.fromCenterBottom(35, -32), Component.literal(menu.outputId.get())).scaled(0.5).noShadow().mainColor(0x7f7f7f7f));

		menu.outputId.whenDataChangedDo(() -> {
			//fluidTextureOutput.addAnimation(new Animation.FadeOut.ToRight(0.3), 0);
			fluidNameTextOutput.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.05);
			fluidIdTextOutput.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.1);

			//fluidTextureOutput = (FluidTextureWidget) productionWindow.addChild(new FluidTextureWidget(Coordinate.fromTopLeft(0, 12), Coordinate.fromTopLeft(10, 10), menu.outputId.get()).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0));
			fluidNameTextOutput = (TextWidget) productionWindow.addChild(new TextWidget(Coordinate.fromCenterBottom(35, -32), menu.outputId.get().isEmpty() ? Component.literal("") :
					Component.translatable(BuiltInRegistries.FLUID.getValue(Identifier.parse(menu.outputId.get())).getFluidType().getDescriptionId())).mainColor(-1).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.05));
			fluidIdTextOutput = (TextWidget) productionWindow.addChild(new TextWidget(Coordinate.fromCenterBottom(38, -32), Component.literal(menu.outputId.get())).scaled(0.5).noShadow().mainColor(0x7f7f7f7f).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.1));
		});
	}
}
