package org.creepebucket.programmable_magic.mananet.machines.consumer.liquid_heater;

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
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.mananet.machines.MachineBlockEntity;
import org.creepebucket.programmable_magic.mananet.machines.RotatableBasicMachine;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;
import org.creepebucket.programmable_magic.registries.ModRecipeTypes;
import org.creepebucket.programmable_magic.utils.Mana;

public class LiquidHeaterBlockEntity extends MachineBlockEntity implements GeoBlockEntity {

	public double powerFact = 1d;
	public boolean inputMode = true;
	public double conversionCost, inputSpeed, outputSpeed;
	public double fuelTotalValue, fuelCurrentValue;
	public double pendingInput, pendingOutput;
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
		pendingInput = input.getDoubleOr("pending_input", 0d);
		pendingOutput = input.getDoubleOr("pending_output", 0d);
		currentFuelId = input.getStringOr("current_fuel_id", "minecraft:air");
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putDouble("power_fact", powerFact);
		output.putBoolean("input_mode", inputMode);
		output.putDouble("fuel_total_value", fuelTotalValue);
		output.putDouble("fuel_current_value", fuelCurrentValue);
		output.putDouble("pending_input", pendingInput);
		output.putDouble("pending_output", pendingOutput);
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
		var facing = level.getBlockState(pos).getValue(RotatableBasicMachine.FACING).getOpposite();
		var pending = pos.relative(facing).above();
		if (level.getBlockEntity(pending) instanceof NetNodeBlockEntity) {
			entity.connect(level, pending, Direction.DOWN, Direction.DOWN);
		}

		var networkData = entity.getNetworkData();
		networkData.setCache(new Mana(2000d, 2000d, 2000d, 2000d));

		if (!entity.enabled) return;

		// 获取流体输入输出端
		var inputHandler = entity.getFluidInput(0);
		var outputHandler = entity.getFluidOutput(0);

		// 查找匹配输入流体的配方
		var inputResource = inputHandler.getResource(0);
		var inputId = BuiltInRegistries.FLUID.getKey(inputResource.getFluid());
		if (!(level instanceof ServerLevel serverLevel)) return;
		var optional = serverLevel.recipeAccess().getRecipeFor(ModRecipeTypes.LIQUID_HEATER.get(), new LiquidHeaterRecipies.Input(inputId), level);
		if (optional.isEmpty()) return;
		var matchedRecipe = optional.get().value();
		entity.conversionCost = matchedRecipe.conversionCost();
		entity.inputId = matchedRecipe.inputFluid();
		entity.outputId = matchedRecipe.outputFluid();
		var convertRatio = matchedRecipe.convertRatio();

		// 检查输出空间
		var outputFluid = BuiltInRegistries.FLUID.getValue(Identifier.parse(matchedRecipe.outputFluid()));
		var outputResource = FluidResource.of(outputFluid);
		try (var transaction = Transaction.openRoot()) {
			if (inputHandler.extract(0, inputResource, 1, transaction) != 1) return;
			if (outputHandler.insert(0, outputResource, (int) convertRatio, transaction) != (int) convertRatio) return;
		}

		// 计算本刻可处理的最大流体量
		var power = 3e6 * entity.powerFact;
		var workFact = entity.powerFact / (1 + 0.015 * (entity.powerFact - 1) * entity.powerFact);
		var load = new Mana(0d, power, 0d, 0d);
		entity.inputSpeed = power / entity.conversionCost * 60;
		entity.outputSpeed = entity.inputSpeed * convertRatio;

		// 双端累积：输入和输出分别记录小数，整数部分才走传输
		if (entity.inputMode) {
			// 魔力模式
			if (!networkData.canProduce(load)) return;
		} else {
			// 燃料模式：热值不够就从物品输入端取新燃料
			if (entity.fuelCurrentValue <= 0) {
				var itemInput = entity.getItemInput(0);
				int fuelSlot = -1;
				int burnTime = 0;
				for (int i = 0; i < itemInput.size(); i++) {
					var itemResource = itemInput.getResource(i);
					if (itemResource.isEmpty()) continue;
					burnTime = itemResource.toStack().getBurnTime(RecipeType.SMELTING, level.fuelValues());
					if (burnTime <= 0) continue;
					fuelSlot = i;
					break;
				}
				if (fuelSlot < 0) return;
				var itemResource = itemInput.getResource(fuelSlot);
				var fuelStack = itemResource.toStack();
				try (var transaction = Transaction.openRoot()) {
					if (itemInput.extract(fuelSlot, itemResource, 1, transaction) != 1) return;
					var remainder = fuelStack.getCraftingRemainder();
					if (remainder != null && itemInput.insert(ItemResource.of(remainder), 1, transaction) != 1) return;
					transaction.commit();
				}
				entity.fuelTotalValue = burnTime * 1e8 / 1600;
				entity.fuelCurrentValue = entity.fuelTotalValue;
				entity.currentFuelId = BuiltInRegistries.ITEM.getKey(itemResource.getItem()).toString();
			}
			// 本刻能处理的流体量（带小数），消耗对应热值
			workFact *= Math.min(1, entity.fuelCurrentValue / (power / 20));
		}
		// 两端取较小值，不足1L就累积等下刻
		var nextPendingInput = entity.pendingInput + entity.inputSpeed * workFact / 1200;
		var nextPendingOutput = entity.pendingOutput + entity.outputSpeed * workFact / 1200;
		var fluidToProcess = (int) Math.min(Math.floor(nextPendingInput), Math.floor(nextPendingOutput * entity.inputSpeed / entity.outputSpeed));
		var fluidToProduce = (int) (fluidToProcess * entity.outputSpeed / entity.inputSpeed);

		// handler传输只支持整数，耗掉整数部分，余数已留在pending里
		if (fluidToProcess > 0) {
			try (var transaction = Transaction.openRoot()) {
				if (outputHandler.insert(0, outputResource, fluidToProduce, transaction) != fluidToProduce) return;
				if (inputHandler.extract(0, inputResource, fluidToProcess, transaction) != fluidToProcess) return;
				transaction.commit();
			}
		}
		entity.pendingInput = nextPendingInput - fluidToProcess;
		entity.pendingOutput = nextPendingOutput - fluidToProduce;
		if (entity.inputMode) networkData.setLoadW(load);
		else entity.fuelCurrentValue -= power / 20 * workFact / (entity.powerFact / (1 + 0.015 * (entity.powerFact - 1) * entity.powerFact));
		entity.setChanged();
	}
}
