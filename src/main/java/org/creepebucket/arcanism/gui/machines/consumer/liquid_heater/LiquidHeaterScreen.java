package org.creepebucket.arcanism.gui.machines.consumer.liquid_heater;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.arcanism.gui.lib.api.Animation;
import org.creepebucket.arcanism.gui.lib.api.DynamicValue;
import org.creepebucket.arcanism.gui.lib.widgets.*;
import org.creepebucket.arcanism.gui.machines.api.MachineScreen;
import org.creepebucket.arcanism.gui.machines.api.MachineWidgets;
import org.creepebucket.arcanism.utils.ModColors;

import static net.minecraft.network.chat.Component.literal;
import static org.creepebucket.arcanism.gui.lib.api.Coordinate.*;
import static org.creepebucket.arcanism.gui.lib.api.ThemeTemplate.*;

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

		var productionWindow = addWidget(new MachineWidgets.InformationWindowWidget(fromCenter(-175, -100), fromTopLeft(350, 70), Component.translatable("gui.arcanism.machine.liquid_heater.window.production"), 0, 0));

		// 头
		productionWindow.addChild(new TextWidget(fromTopLeft(7, 39), Component.translatable("gui.arcanism.machine.label.machine_type")).bottomAlignY().applyTheme(LABEL_TEXT));
		productionWindow.addChild(new TextWidget(fromTopLeft(7, 15), Component.translatable("gui.arcanism.machine.type.liquid_heater")).scaled(1.5));
		productionWindow.addChild(new TextWidget(fromTopRight(-7, 39), Component.translatable("gui.arcanism.machine.label.mana_type")).rightAlign().bottomAlignY().applyTheme(LABEL_TEXT));
		productionWindow.addChild(new TextWidget(fromTopRight(-7, 15), Component.translatable("gui.arcanism.machine.mana_type.temperature")).scaled(1.5).rightAlign());

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
		fluidIdTextInput = (TextWidget) productionWindow.addChild(new TextWidget(fromCenterBottom(-107, -32), literal(menu.inputId.get())).applyTheme(HALF_LABEL));

		menu.inputId.whenDataChangedDo(() -> {
			//fluidTextureInput.addAnimation(new Animation.FadeOut.ToRight(0.3), 0);
			fluidNameTextInput.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.05);
			fluidIdTextInput.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.1);

			//fluidTextureInput = (FluidTextureWidget) productionWindow.addChild(new FluidTextureWidget(fromTopLeft(0, 12), fromTopLeft(10, 10), menu.inputId.get()).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0));
			fluidNameTextInput = (TextWidget) productionWindow.addChild(new TextWidget(fromCenterBottom(-110, -32), menu.inputId.get().isEmpty() ? literal("") :
					Component.translatable(BuiltInRegistries.FLUID.getValue(Identifier.parse(menu.inputId.get())).getFluidType().getDescriptionId())).mainColor(-1).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.05));
			fluidIdTextInput = (TextWidget) productionWindow.addChild(new TextWidget(fromCenterBottom(-107, -32), literal(menu.inputId.get())).applyTheme(HALF_LABEL).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.1));
		});

		// 输出
		productionWindow.addChild(new NumberDisplayWidget(fromCenterBottom(105, -30), menu.outputSpeed, 6, 2, true).rightAlign());
		productionWindow.addChild(new TextWidget(fromCenterBottom(108, -18), literal("L/min")).rightAlign().mainColor(-1));

		// fluidTextureOutput = (FluidTextureWidget) productionWindow.addChild(new FluidTextureWidget(fromCenterBottom(-75, -33), fromTopLeft(10, 10), menu.outputId.get()).rightAlign());
		fluidNameTextOutput = (TextWidget) productionWindow.addChild(new TextWidget(fromCenterBottom(35, -32), menu.outputId.get().isEmpty() ? literal("") :
				Component.translatable(BuiltInRegistries.FLUID.getValue(Identifier.parse(menu.outputId.get())).getFluidType().getDescriptionId())).mainColor(-1));
		fluidIdTextOutput = (TextWidget) productionWindow.addChild(new TextWidget(fromCenterBottom(35, -32), literal(menu.outputId.get())).applyTheme(HALF_LABEL));

		menu.outputId.whenDataChangedDo(() -> {
			//fluidTextureOutput.addAnimation(new Animation.FadeOut.ToRight(0.3), 0);
			fluidNameTextOutput.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.05);
			fluidIdTextOutput.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.1);

			//fluidTextureOutput = (FluidTextureWidget) productionWindow.addChild(new FluidTextureWidget(fromTopLeft(0, 12), fromTopLeft(10, 10), menu.outputId.get()).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0));
			fluidNameTextOutput = (TextWidget) productionWindow.addChild(new TextWidget(fromCenterBottom(35, -32), menu.outputId.get().isEmpty() ? literal("") :
					Component.translatable(BuiltInRegistries.FLUID.getValue(Identifier.parse(menu.outputId.get())).getFluidType().getDescriptionId())).mainColor(-1).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.05));
			fluidIdTextOutput = (TextWidget) productionWindow.addChild(new TextWidget(fromCenterBottom(38, -32), literal(menu.outputId.get())).applyTheme(HALF_LABEL).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.1));
		});

		// ================= 燃料窗口 ================= //

		var fuelWindow = addWidget(new MachineWidgets.InformationWindowWidget(fromCenter(-175, -20), fromTopLeft(170, 100), Component.translatable("gui.arcanism.machine.liquid_heater.window.fuel"), 0 ,0));

		fuelWindow.addChild(new TextWidget(fromTopLeft(7, 18), Component.translatable("gui.arcanism.machine.liquid_heater.label.current_fuel")).applyTheme(DIM_TEXT));
		fuelWindow.addChild(new RectangleWidget(fromTopLeft(7, 28), fromTopLeft(8, 16)).mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)));
		var texture = (ItemTextureWidget) fuelWindow.addChild(new ItemTextureWidget(fromTopLeft(8, 28), fromTopLeft(16, 16), menu.currentFuelId.get()));
		fuelNameText = (TextWidget) fuelWindow.addChild(new TextWidget(fromTopLeft(16, 29), Component.translatable(BuiltInRegistries.ITEM.getValue(Identifier.parse(menu.currentFuelId.get())).getDescriptionId())).applyTheme(GENERAL_TEXT));
		fuelIdText = (TextWidget) fuelWindow.addChild(new TextWidget(fromTopLeft(16, 39), Component.literal(menu.currentFuelId.get())).applyTheme(HALF_LABEL));
		menu.currentFuelId.whenDataChangedDo(() -> {
			fuelNameText.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.05);
			fuelIdText.addAnimation(new Animation.FadeOut.ToRight(0.3), 0.1);

			texture.itemId = menu.currentFuelId.get();
			fuelNameText = (TextWidget) fuelWindow.addChild(new TextWidget(fromTopLeft(16, 29), Component.translatable(BuiltInRegistries.ITEM.getValue(Identifier.parse(menu.currentFuelId.get())).getDescriptionId())).applyTheme(GENERAL_TEXT).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.05));
			fuelIdText = (TextWidget) fuelWindow.addChild(new TextWidget(fromTopLeft(16, 39), literal(menu.currentFuelId.get())).applyTheme(HALF_LABEL).addAnimation(new Animation.FadeIn.FromLeft(0.3), 0.1));
		});

		fuelWindow.addChild(new TextWidget(fromCenterTop(0, 18), Component.translatable("gui.arcanism.machine.liquid_heater.label.fuel_heat_value")).applyTheme(DIM_TEXT));
		fuelWindow.addChild(new RectangleWidget(fromCenterTop(0, 28), fromTopLeft(8, 16)).mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)));
		var unit = new TextSwitchWidget(fromCenterTop(45, 34), fromTopLeft(13, 9), 1, "J").mainColor(ModColors.MAIN_COLOR_T.withAlpha(0xbf)).bgColor(0);
		fuelWindow.addChild(new NumberDisplayWidget(fromCenterTop(0, 28), menu.fuelTotalValue, 5, 1.5, (TextSwitchWidget) unit, "J", true).mainColor(-1));
		fuelWindow.addChild(unit);

		fuelWindow.addChild(new RectangleWidget(fromBottomLeft(7, -35), fromTopLeft(8, 16)).mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)));
		fuelWindow.addChild(new TextWidget(fromBottomLeft(7, -45), Component.translatable("gui.arcanism.machine.liquid_heater.label.burn_time")).applyTheme(DIM_TEXT));
		fuelWindow.addChild(new NumberDisplayWidget(fromBottomLeft(7, -35),DynamicValue.fromSupplier(() -> menu.fuelCurrentValue.get() / (4e6 * 5 * (Math.pow(menu.powerFact.get() + 0.5, 2) - 0.25))), 5, 1.5, true).mainColor(-1));
		fuelWindow.addChild(new TextWidget(fromBottomLeft(53, -28), literal("s")).noShadow().mainColor(ModColors.MAIN_COLOR_T.withAlpha(0xbf)));

		fuelWindow.addChild(new RectangleWidget(fromCenterBottom(0, -35), fromTopLeft(8, 16)).mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)));
		fuelWindow.addChild(new TextWidget(fromCenterBottom(0, -45), Component.translatable("gui.arcanism.machine.liquid_heater.label.remaining_heat")).applyTheme(DIM_TEXT));
		var currentUnit = new TextSwitchWidget(fromCenterBottom(45, -29), fromTopLeft(13, 9), 1, "J").mainColor(ModColors.MAIN_COLOR_T.withAlpha(0xbf)).bgColor(0);
		fuelWindow.addChild(new NumberDisplayWidget(fromCenterBottom(0, -35), menu.fuelCurrentValue, 5, 1.5, (TextSwitchWidget) currentUnit, "J", true).mainColor(-1));
		fuelWindow.addChild(currentUnit);

		fuelWindow.addChild(new ProgressBarWidget(fromBottomLeft(7, -16), fromTopRight(-14, 9), menu.fuelCurrentValue, menu.fuelTotalValue).mainColor(-1));

		// ================= 超频窗口 ================= //

		var overclockWindow = addWidget(new MachineWidgets.InformationWindowWidget(fromCenter(5, -20), fromTopLeft(170, 50), Component.translatable("gui.arcanism.machine.window.power_control"), 0 ,0));

		overclockWindow.addChild(new RectangleWidget(fromTopLeft(6, 25), fromTopLeft(75, 2)).mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)));
		overclockWindow.addChild(new TextWidget(fromTopLeft(7, 20), Component.translatable("gui.arcanism.machine.label.overclock_factor")).applyTheme(GENERAL_TEXT));
		overclockWindow.addChild(new NumberDisplayWidget(fromTopLeft(44, 16), menu.powerFact, 4, 1.5, true).mainColor(-1));

		overclockWindow.addChild(new TextWidget(fromTopRight(-45, 24), Component.translatable("gui.arcanism.machine.label.expected_power")).scaled(0.5).rightAlign().applyTheme(DIM_TEXT));
		overclockWindow.addChild(new NumberDisplayWidget(fromTopRight(-45, 16), DynamicValue.fromSupplier(() -> 4e6 * 5 * (Math.pow(menu.powerFact.get() + 0.5, 2) - 0.25)), 6, 1, true).rightAlign().mainColor(-1));

		overclockWindow.addChild(new RectangleWidget(fromTopRight(-6, 16), fromTopLeft(37, 9)).mainColor(ModColors.MAIN_COLOR_T.withAlpha(0x7f)).rightAlign());
		overclockWindow.addChild(new TextWidget(fromTopRight(-7, 24), Component.translatable("gui.arcanism.machine.label.effective_power")).scaled(0.5).rightAlign().applyTheme(DIM_TEXT));
		overclockWindow.addChild(new NumberDisplayWidget(fromTopRight(-7, 16), DynamicValue.fromSupplier(() -> 4e6 * menu.powerFact.get()), 6, 1, true).rightAlign().mainColor(-1));

		// 添加刻度
		for (int i = 1; i < 5; i++) {
			overclockWindow.addChild(new TextWidget(custom((double) i / 5, (int) Math.floor(-14 * (double) i / 5 + 7), 1, -13), literal(String.valueOf(i))).scaled(0.5).centerAlign().applyTheme(DIM_TEXT));
		}
		overclockWindow.addChild(new TextWidget(custom(0, 7, 1, -13), literal("0")).scaled(0.5).applyTheme(DIM_TEXT));
		overclockWindow.addChild(new TextWidget(custom(1, -6, 1, -13), literal(String.valueOf(5))).scaled(0.5).rightAlign().applyTheme(DIM_TEXT));

		overclockWindow.addChild(new ThinSlideBarWidget(fromBottomLeft(7, -9), fromTopRight(-14, 5), 0, 5, menu.powerFact).step(0.05).bgColor(-1));

		// ================= 控制窗口 ================= //

		var controlWindow = addWidget(new MachineWidgets.MachineControlWindow(fromCenter(5, 40), fromTopLeft(170, 40), menu));

		controlWindow.addChild(new SwitchWidget(fromTopRight(-7, 16), fromTopLeft(50, 19), Component.translatable("gui.arcanism.machine.liquid_heater.switch.fuel"), Component.translatable("gui.arcanism.machine.liquid_heater.switch.mana"), menu.inputMode).rightAlign());
	}
}
