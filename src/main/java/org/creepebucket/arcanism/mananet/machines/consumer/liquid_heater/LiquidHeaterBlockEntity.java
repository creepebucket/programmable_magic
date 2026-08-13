package org.creepebucket.arcanism.mananet.machines.consumer.liquid_heater;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.creepebucket.arcanism.ModConfig;
import org.creepebucket.arcanism.mananet.NetNodeBlockEntity;
import org.creepebucket.arcanism.mananet.machines.MachineBlockEntity;
import org.creepebucket.arcanism.mananet.machines.BasicMachine;
import org.creepebucket.arcanism.utils.RelativeBlockPos;
import org.creepebucket.arcanism.registries.ModBlockEntities;
import org.creepebucket.arcanism.registries.ModRecipeTypes;
import org.creepebucket.arcanism.utils.Mana;

public class LiquidHeaterBlockEntity extends MachineBlockEntity implements GeoBlockEntity {

	public double powerFact = 1d;
	public boolean inputMode = true;
	public double conversionCost, inputSpeed, outputSpeed;
	public double fuelTotalValue, fuelCurrentValue;
	public double recipeProgress;
	public String inputId = "", outputId = "", currentFuelId = "";

	public final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

	public LiquidHeaterBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.LIQUID_HEATER_BLOCK_ENTITY.get(), pos, state);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		powerFact = input.getDoubleOr("power_fact", 1d);
		inputMode = input.getBooleanOr("input_mode", true);
		fuelTotalValue = input.getDoubleOr("fuel_total_value", 0d);
		fuelCurrentValue = input.getDoubleOr("fuel_current_value", 0d);
		recipeProgress = input.getDoubleOr("recipe_progress", 0d);
		inputId = input.getStringOr("input_id", "minecraft:air");
		currentFuelId = input.getStringOr("current_fuel_id", "minecraft:air");
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putDouble("power_fact", powerFact);
		output.putBoolean("input_mode", inputMode);
		output.putDouble("fuel_total_value", fuelTotalValue);
		output.putDouble("fuel_current_value", fuelCurrentValue);
		output.putDouble("recipe_progress", recipeProgress);
		output.putString("input_id", inputId);
		output.putString("current_fuel_id", currentFuelId);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return geoCache;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, LiquidHeaterBlockEntity entity) {
		if (level.isClientSide()) return;

		// 连接到魔力网络
		var facing = level.getBlockState(pos).getValue(BasicMachine.FACING).getOpposite();
		var pending = pos.relative(facing).above();
		if (level.getBlockEntity(pending) instanceof NetNodeBlockEntity) {
			entity.connect(level, pending, Direction.DOWN, Direction.DOWN);
		}

		var networkData = entity.getNetworkData();
		networkData.setCache(new Mana(2000d, 2000d, 2000d, 2000d));

		if (!entity.enabled) return;

		// 获取输入输出端
		var block = (BasicMachine) entity.getBlockState().getBlock();
		RelativeBlockPos fluidInputPos = null, fluidOutputPos = null, itemInputPos = null;
		for (var entry : block.IO_DEFINITION.entrySet()) {
			switch (entry.getValue()) {
				case "fluid_input" -> fluidInputPos = entry.getKey();
				case "fluid_output" -> fluidOutputPos = entry.getKey();
				case "item_input" -> itemInputPos = entry.getKey();
			}
		}
		var fluidInput = fluidInputPos != null ? entity.fluidStorage.get(fluidInputPos) : null;
		var fluidOutput = fluidOutputPos != null ? entity.fluidStorage.get(fluidOutputPos) : null;
		var itemInput = itemInputPos != null ? entity.itemStorage.get(itemInputPos) : null;

		// 查找匹配输入流体的配方
		// 屎
		var inputResource = fluidInput.getResource(0);
		var inputId = entity.recipeProgress > 0 ? Identifier.parse(entity.inputId) : BuiltInRegistries.FLUID.getKey(inputResource.getFluid());
		if (!(level instanceof ServerLevel serverLevel)) return;
		var optional = serverLevel.recipeAccess().getRecipeFor(ModRecipeTypes.LIQUID_HEATER.get(), new LiquidHeaterRecipies.Input(inputId), level);
		if (optional.isEmpty()) return;
		var matchedRecipe = optional.get().value();
		entity.conversionCost = matchedRecipe.conversionCost();
		entity.inputId = matchedRecipe.inputFluid();
		entity.outputId = matchedRecipe.outputFluid();
		var convertRatio = matchedRecipe.convertRatio();
		inputResource = FluidResource.of(BuiltInRegistries.FLUID.getValue(Identifier.parse(entity.inputId)));
		var outputResource = FluidResource.of(BuiltInRegistries.FLUID.getValue(Identifier.parse(entity.outputId)));
		double energyCost = entity.conversionCost * 5 * (entity.powerFact + 1);

		// 从输入/输出侧获取本刻最高可处理数量
		double maximumProcessCapacityFromInput = Math.max(0d, fluidInput.getAmountAsInt(0) - entity.recipeProgress);
		double maximumProcessCapacityFromOutput = Math.max(0d, Math.floor((fluidOutput.getCapacityAsInt(0, outputResource) - fluidOutput.getAmountAsInt(0)) / convertRatio) - entity.recipeProgress);

		// 从机器功率侧获取本刻最高可处理数量
		double maximumProcessCapacityFromMachine = entity.powerFact * 4e6 / 20 / entity.conversionCost;
		// 从能源侧获取本刻最高可处理数量
		double maximumProcessCapacityFromEnergy = entity.fuelCurrentValue / energyCost;

		// 计划消耗的燃料
		int[] fuelAmounts = new int[itemInput.size()];
		// 计划消耗的燃料热值
		double addedFuelValue = 0;
		// 当前燃料
		String currentFuelId = entity.currentFuelId;

		// 计算当前处理量
		double maximumProcessCapacity = Math.min(maximumProcessCapacityFromMachine, Math.min(maximumProcessCapacityFromInput, maximumProcessCapacityFromOutput));

		if (entity.inputMode) {
			// 魔力模式
			maximumProcessCapacityFromEnergy = networkData.getNext().getTemperature() / energyCost;
		} else {
			// 燃料模式
			for (int i = 0; i < itemInput.size(); i++) {
				// 获取燃烧时间
				var fuel = itemInput.getResource(i);
				// 能烧吗
				if (fuel.isEmpty() || fuel.toStack().getBurnTime(RecipeType.SMELTING, level.fuelValues()) <= 0) continue;
				// 如果处理能力已经达到了 就跳过燃料计算
				if (maximumProcessCapacityFromEnergy >= maximumProcessCapacity) break;

				double fuelValue = fuel.toStack().getBurnTime(RecipeType.SMELTING, level.fuelValues()) * ModConfig.CONFIG.fuelValueMultiplier.get();

				// 预取燃料
				fuelAmounts[i] = Math.min(itemInput.getAmountAsInt(i), (int) Math.ceil((maximumProcessCapacity * energyCost - entity.fuelCurrentValue - addedFuelValue) / fuelValue));
				addedFuelValue += fuelAmounts[i] * fuelValue;

				// 更新最大处理量
				maximumProcessCapacityFromEnergy = (entity.fuelCurrentValue + addedFuelValue) / energyCost;
				// 更新当前燃料
				currentFuelId = BuiltInRegistries.ITEM.getKey(fuel.getItem()).toString();
			}
		}

		// 计算最高的本刻处理量
		maximumProcessCapacity = Math.min(maximumProcessCapacityFromEnergy, maximumProcessCapacity);

		// 根据本刻处理量计算每分钟处理量
		entity.inputSpeed = maximumProcessCapacity * 1200;
		entity.outputSpeed = entity.inputSpeed * convertRatio;

		// 执行前检测
		if (maximumProcessCapacity <= 0) return;

		// 执行逻辑
		// 计算待输入/输出的流体 (并舍入)
		int fluidToProcess = (int) Math.floor(entity.recipeProgress + maximumProcessCapacity);
		int fluidToProduce = (int) Math.floor(fluidToProcess * convertRatio);

		// 预期功率计算
		double energyConsumption = maximumProcessCapacity * energyCost;

		// 实际消耗燃料
		try (var transaction = Transaction.openRoot()) {
			for (int i = 0; i < fuelAmounts.length; i++) {
				if (fuelAmounts[i] <= 0) continue;
				var fuel = itemInput.getResource(i);

				// 根据先前计算取出燃料
				if (itemInput.extract(i, fuel, fuelAmounts[i], transaction) != fuelAmounts[i]) return;
				// 获取可能的燃烧后剩余物品(如空桶)
				var remainder = fuel.toStack().getCraftingRemainder();
				if (remainder != null && itemInput.insert(ItemResource.of(remainder), fuelAmounts[i] * remainder.count(), transaction) != fuelAmounts[i] * remainder.count()) return;
			}

			if (fluidOutput.insert(0, outputResource, fluidToProduce, transaction) != fluidToProduce) return;
			if (fluidToProcess > 0 && fluidInput.extract(0, inputResource, fluidToProcess, transaction) != fluidToProcess) return;
			transaction.commit();
		}

		// 根据实际存取的流体计算剩余流体
		entity.recipeProgress += maximumProcessCapacity - fluidToProcess;

		// 如果是魔力模式 就扣魔力
		if (entity.inputMode) networkData.setLoadW(new Mana(0d, energyConsumption * 20, 0d, 0d));
		else {
			// 如果是燃料模式 计算燃料信息
			entity.fuelTotalValue = addedFuelValue > 0 ? addedFuelValue : entity.fuelTotalValue;
			entity.fuelCurrentValue += addedFuelValue - energyConsumption;
			entity.currentFuelId = currentFuelId;
		}
		entity.setChanged();
	}
}
