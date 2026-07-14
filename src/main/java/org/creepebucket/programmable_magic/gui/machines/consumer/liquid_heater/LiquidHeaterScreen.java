package org.creepebucket.programmable_magic.gui.machines.consumer.liquid_heater;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.lib.api.Animation;
import org.creepebucket.programmable_magic.gui.lib.api.DynamicValue;
import org.creepebucket.programmable_magic.gui.lib.widgets.*;
import org.creepebucket.programmable_magic.gui.machines.api.MachineScreen;
import org.creepebucket.programmable_magic.gui.machines.api.MachineWidgets;
import org.creepebucket.programmable_magic.utils.ModColors;

import static net.minecraft.network.chat.Component.literal;
import static org.creepebucket.programmable_magic.gui.lib.api.Coordinate.*;

public class LiquidHeaterScreen extends MachineScreen<LiquidHeaterMenu> {
	public FluidTextureWidget fluidTextureInput, fluidTextureOutput;
	public TextWidget fluidNameTextInput, fluidIdTextInput, fluidNameTextOutput, fluidIdTextOutput, fuelNameText, fuelIdText;

	public LiquidHeaterScreen(LiquidHeaterMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}

	@Override
	public void buildWidget() {
		super.buildWidget();

		root.mainColor(ModColors.MAIN_COLOR_T);

		// ================= 生产窗口 ================= //

		var productionWindow = addWidget(new MachineWidgets.InformationWindowWidget(fromCenter(-175, -100), fromTopLeft(350, 70), literal("生产总览"), 0, 0));

		// 头
		productionWindow.addChild(new TextWidget(fromTopLeft(7, 39), literal("机器类型")).noShadow().bottomAlignY().mainColor(0xff7f7f7f));
		productionWindow.addChild(new TextWidget(fromTopLeft(7, 15), literal("液体加热器")).scaled(1.5));
		productionWindow.addChild(new TextWidget(fromTopRight(-7, 39), literal("魔力类型")).noShadow().rightAlign().bottomAlignY().mainColor(0xff7f7f7f));
		productionWindow.addChild(new TextWidget(fromTopRight(-7, 15), literal("温度/Tem")).scaled(1.5).rightAlign());

		// 转换能量
		productionWindow.addChild(new TextWidget(fromCenterBottom(0, -5), literal(">>>")).noShadow().scaled(3).bottomAlignY().centerAlign().mainColor(0x1fffffff));
		var conversionCostUnit = productionWindow.addChild(new TextWidget(fromCenterBottom(8, -25), literal("J/L")).noShadow().mainColor(0xbfffffff));
		conversionCostUnit.addChild(new NumberDisplayWidget(fromTopLeft(-1, -1), menu.conversionCost, 6, 1, true).mainColor(0xbfffffff).rightAlign());

		// 输入
		productionWindow.addChild(new NumberDisplayWidget(fromCenterBottom(-40, -30), menu.inputSpeed, 6, 2, true).rightAlign());
		productionWindow.addChild(new TextWidget(fromCenterBottom(-37, -18), literal("L/min")).rightAlign().mainColor(-1));

		// fluidTextureInput = (FluidTextureWidget) productionWindow.addChild(new FluidTextureWidget(fromCenterBottom(-75, -33), fromTopLeft(10, 10), menu.inputId.get()).rightAlign());
		fluidNameTextInput = (TextWidget) productionWindow.addChild(new TextWidget(fromCenterBottom(-110, -32), menu.inputId.get().isEmpty() ? literal("") :
				Component.translatable(BuiltInRegistries.FLUID.getValue(Identifier.parse(menu.inputId.get())).getFluidType().getDescriptionId())).mainColor(-1));
		fluidIdTextInput = (TextWidget) productionWindow.addChild(new TextWidget(fromCenterBottom(-107, -32), literal(menu.inputId.get())).scaled(0.5).noShadow().mainColor(0x7f7f7f7f));

		menu.inputId.whenDataChangedDo(() -> {
			//fluidTextureInput.addAnimation(new Animation.FadeOut.ToRight(0.3), 0);
			fluidNameTextInput.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.05);
			fluidIdTextInput.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.1);

			//fluidTextureInput = (FluidTextureWidget) productionWindow.addChild(new FluidTextureWidget(fromTopLeft(0, 12), fromTopLeft(10, 10), menu.inputId.get()).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0));
			fluidNameTextInput = (TextWidget) productionWindow.addChild(new TextWidget(fromCenterBottom(-110, -32), menu.inputId.get().isEmpty() ? literal("") :
					Component.translatable(BuiltInRegistries.FLUID.getValue(Identifier.parse(menu.inputId.get())).getFluidType().getDescriptionId())).mainColor(-1).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.05));
			fluidIdTextInput = (TextWidget) productionWindow.addChild(new TextWidget(fromCenterBottom(-107, -32), literal(menu.inputId.get())).scaled(0.5).noShadow().mainColor(0x7f7f7f7f).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.1));
		});

		// 输出
		productionWindow.addChild(new NumberDisplayWidget(fromCenterBottom(105, -30), menu.outputSpeed, 6, 2, true).rightAlign());
		productionWindow.addChild(new TextWidget(fromCenterBottom(108, -18), literal("L/min")).rightAlign().mainColor(-1));

		// fluidTextureOutput = (FluidTextureWidget) productionWindow.addChild(new FluidTextureWidget(fromCenterBottom(-75, -33), fromTopLeft(10, 10), menu.outputId.get()).rightAlign());
		fluidNameTextOutput = (TextWidget) productionWindow.addChild(new TextWidget(fromCenterBottom(35, -32), menu.outputId.get().isEmpty() ? literal("") :
				Component.translatable(BuiltInRegistries.FLUID.getValue(Identifier.parse(menu.outputId.get())).getFluidType().getDescriptionId())).mainColor(-1));
		fluidIdTextOutput = (TextWidget) productionWindow.addChild(new TextWidget(fromCenterBottom(35, -32), literal(menu.outputId.get())).scaled(0.5).noShadow().mainColor(0x7f7f7f7f));

		menu.outputId.whenDataChangedDo(() -> {
			//fluidTextureOutput.addAnimation(new Animation.FadeOut.ToRight(0.3), 0);
			fluidNameTextOutput.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.05);
			fluidIdTextOutput.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.1);

			//fluidTextureOutput = (FluidTextureWidget) productionWindow.addChild(new FluidTextureWidget(fromTopLeft(0, 12), fromTopLeft(10, 10), menu.outputId.get()).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0));
			fluidNameTextOutput = (TextWidget) productionWindow.addChild(new TextWidget(fromCenterBottom(35, -32), menu.outputId.get().isEmpty() ? literal("") :
					Component.translatable(BuiltInRegistries.FLUID.getValue(Identifier.parse(menu.outputId.get())).getFluidType().getDescriptionId())).mainColor(-1).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.05));
			fluidIdTextOutput = (TextWidget) productionWindow.addChild(new TextWidget(fromCenterBottom(38, -32), literal(menu.outputId.get())).scaled(0.5).noShadow().mainColor(0x7f7f7f7f).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.1));
		});

		// ================= 燃料窗口 ================= //

		var fuelWindow = addWidget(new MachineWidgets.InformationWindowWidget(fromCenter(-175, -20), fromTopLeft(170, 100), literal("燃料信息"), 0 ,0));

		fuelWindow.addChild(new TextWidget(fromTopLeft(7, 18), literal("当前燃料")).noShadow().mainColor(0x7fffffff));
		fuelWindow.addChild(new RectangleWidget(fromTopLeft(7, 28), fromTopLeft(8, 16)).mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)));
		var texture = (ItemTextureWidget) fuelWindow.addChild(new ItemTextureWidget(fromTopLeft(8, 28), fromTopLeft(16, 16), menu.currentFuelId.get()));
		fuelNameText = (TextWidget) fuelWindow.addChild(new TextWidget(fromTopLeft(16, 29), Component.translatable(BuiltInRegistries.ITEM.getValue(Identifier.parse(menu.currentFuelId.get())).getDescriptionId())).noShadow().mainColor(-1));
		fuelIdText = (TextWidget) fuelWindow.addChild(new TextWidget(fromTopLeft(16, 39), Component.literal(menu.currentFuelId.get())).noShadow().scaled(0.5).mainColor(0x7f7f7f7f));
		menu.currentFuelId.whenDataChangedDo(() -> {
			fuelNameText.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.05);
			fuelIdText.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.1);

			texture.itemId = menu.currentFuelId.get();
			fuelNameText = (TextWidget) fuelWindow.addChild(new TextWidget(fromTopLeft(16, 29), Component.translatable(BuiltInRegistries.ITEM.getValue(Identifier.parse(menu.currentFuelId.get())).getDescriptionId())).noShadow().mainColor(-1).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.05));
			fuelIdText = (TextWidget) fuelWindow.addChild(new TextWidget(fromTopLeft(16, 39), literal(menu.currentFuelId.get())).noShadow().scaled(0.5).mainColor(0x7f7f7f7f).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.1));
		});

		fuelWindow.addChild(new TextWidget(fromCenterTop(0, 18), literal("燃料热值")).noShadow().mainColor(0x7fffffff));
		fuelWindow.addChild(new RectangleWidget(fromCenterTop(0, 28), fromTopLeft(8, 16)).mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)));
		var unit = new TextSwitchWidget(fromCenterTop(45, 34), fromTopLeft(13, 9), 1, "J").mainColor(ModColors.MAIN_COLOR_T.withAlpha(0xbf)).bgColor(0);
		fuelWindow.addChild(new NumberDisplayWidget(fromCenterTop(0, 28), menu.fuelTotalValue, 5, 1.5, (TextSwitchWidget) unit, "J", true).mainColor(-1));
		fuelWindow.addChild(unit);

		fuelWindow.addChild(new RectangleWidget(fromBottomLeft(7, -35), fromTopLeft(8, 16)).mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)));
		fuelWindow.addChild(new TextWidget(fromBottomLeft(7, -45), literal("燃烧时间")).noShadow().mainColor(0x7fffffff));
		fuelWindow.addChild(new NumberDisplayWidget(fromBottomLeft(7, -35),DynamicValue.fromSupplier(() -> menu.fuelCurrentValue.get() / (4e6 * (Math.pow(menu.powerFact.get() + 1, Math.log10(3) * Math.log10(10) / Math.log10(2)) - 1))), 5, 1.5, true).mainColor(-1));
		fuelWindow.addChild(new TextWidget(fromBottomLeft(53, -28), literal("s")).noShadow().mainColor(ModColors.MAIN_COLOR_T.withAlpha(0xbf)));

		fuelWindow.addChild(new RectangleWidget(fromCenterBottom(0, -35), fromTopLeft(8, 16)).mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)));
		fuelWindow.addChild(new TextWidget(fromCenterBottom(0, -45), literal("剩余热值")).noShadow().mainColor(0x7fffffff));
		var currentUnit = new TextSwitchWidget(fromCenterBottom(45, -29), fromTopLeft(13, 9), 1, "J").mainColor(ModColors.MAIN_COLOR_T.withAlpha(0xbf)).bgColor(0);
		fuelWindow.addChild(new NumberDisplayWidget(fromCenterBottom(0, -35), menu.fuelCurrentValue, 5, 1.5, (TextSwitchWidget) currentUnit, "J", true).mainColor(-1));
		fuelWindow.addChild(currentUnit);

		fuelWindow.addChild(new ProgressBarWidget(fromBottomLeft(7, -16), fromTopRight(-14, 9), menu.fuelCurrentValue, menu.fuelTotalValue).mainColor(-1));

		// ================= 超频窗口 ================= //

		var overclockWindow = addWidget(new MachineWidgets.InformationWindowWidget(fromCenter(5, -20), fromTopLeft(170, 50), literal("功率控制"), 0 ,0));

		overclockWindow.addChild(new RectangleWidget(fromTopLeft(6, 25), fromTopLeft(75, 2)).mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)));
		overclockWindow.addChild(new TextWidget(fromTopLeft(7, 20), literal("超频倍率")).noShadow().mainColor(-1));
		overclockWindow.addChild(new NumberDisplayWidget(fromTopLeft(44, 16), menu.powerFact, 4, 1.5, true).mainColor(-1));

		overclockWindow.addChild(new TextWidget(fromTopRight(-45, 24), literal("预期功率 / W")).scaled(0.5).noShadow().rightAlign().mainColor(0x7fffffff));
		overclockWindow.addChild(new NumberDisplayWidget(fromTopRight(-45, 16), DynamicValue.fromSupplier(() -> 4e6 * (Math.pow(menu.powerFact.get() + 1, Math.log10(3) * Math.log10(10) / Math.log10(2)) - 1)), 6, 1, true).rightAlign().mainColor(-1));

		overclockWindow.addChild(new RectangleWidget(fromTopRight(-6, 16), fromTopLeft(37, 9)).mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)).rightAlign());
		overclockWindow.addChild(new TextWidget(fromTopRight(-7, 24), literal("有效功率 / W")).scaled(0.5).noShadow().rightAlign().mainColor(0x7fffffff));
		overclockWindow.addChild(new NumberDisplayWidget(fromTopRight(-7, 16), DynamicValue.fromSupplier(() -> 4e6 * menu.powerFact.get()), 6, 1, true).rightAlign().mainColor(-1));

		// 添加刻度
		for (int i = 1; i < 5; i++) {
			overclockWindow.addChild(new TextWidget(custom((double) i / 5, (int) Math.floor(-14 * (double) i / 5 + 7), 1, -13), literal(String.valueOf(i))).scaled(0.5).noShadow().centerAlign().mainColor(0x7fffffff));
		}
		overclockWindow.addChild(new TextWidget(custom(0, 7, 1, -13), literal("0")).scaled(0.5).noShadow().mainColor(0x7fffffff));
		overclockWindow.addChild(new TextWidget(custom(1, -6, 1, -13), literal(String.valueOf(5))).scaled(0.5).noShadow().rightAlign().mainColor(0x7fffffff));

		overclockWindow.addChild(new ThinSlideBarWidget(fromBottomLeft(7, -9), fromTopRight(-14, 5), 0, 5, menu.powerFact).step(0.05).bgColor(-1));

		// ================= 控制窗口 ================= //

		var controlWindow = addWidget(new MachineWidgets.MachineControlWindow(fromCenter(5, 40), fromTopLeft(170, 40), menu));

		controlWindow.addChild(new SwitchWidget(fromTopRight(-7, 16), fromTopLeft(50, 19), literal("燃料"), literal("魔力"), menu.inputMode).rightAlign());
	}
}
