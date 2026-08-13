package org.creepebucket.arcanism.mananet.machines.generator.pressure_relief_valve;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.creepebucket.arcanism.mananet.NetNodeBlockEntity;
import org.creepebucket.arcanism.mananet.machines.BasicMachine;
import org.creepebucket.arcanism.mananet.machines.MachineBlockEntity;
import org.creepebucket.arcanism.registries.ModBlockEntities;
import org.creepebucket.arcanism.registries.ModRecipeTypes;
import org.creepebucket.arcanism.utils.Mana;
import org.creepebucket.arcanism.utils.RelativeBlockPos;

public class PressureReliefValveBlockEntity extends MachineBlockEntity implements GeoBlockEntity {

	public double powerFact = 1d;
	public double unoutputtedMana;
	public double manaPowerW;
	public String recipeInputId = "";

	public final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

	public PressureReliefValveBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.PRESSURE_RELIEF_VALVE_BLOCK_ENTITY.get(), pos, state);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		powerFact = input.getDoubleOr("power_fact", 1d);
		unoutputtedMana = input.getDoubleOr("unoutputted_mana", 0d);
		recipeInputId = input.getStringOr("recipe_input_id", "");
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putDouble("power_fact", powerFact);
		output.putDouble("unoutputted_mana", unoutputtedMana);
		output.putString("recipe_input_id", recipeInputId);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return geoCache;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, PressureReliefValveBlockEntity entity) {
		if (level.isClientSide()) return;

		// 连接到魔力网络
		var facing = level.getBlockState(pos).getValue(BasicMachine.FACING).getOpposite();
		var pending = pos.relative(facing).above();
		if (level.getBlockEntity(pending) instanceof NetNodeBlockEntity) {
			entity.connect(level, pending, Direction.DOWN, Direction.DOWN);
		}

		// 设置缓存上限
		var networkData = entity.getNetworkData();
		networkData.setCache(new Mana(2000d, 2000d, 2000d, 200000000d));

		entity.manaPowerW = 0;

		if (!entity.enabled) return;

		// 获取流体IO
		var block = (BasicMachine) entity.getBlockState().getBlock();
		RelativeBlockPos fluidInputPos = null;
		for (var entry : block.IO_DEFINITION.entrySet()) {
			if (entry.getValue().equals("fluid_input")) fluidInputPos = entry.getKey();
		}
		var fluidInput = fluidInputPos != null ? entity.fluidStorage.get(fluidInputPos) : null;
		if (fluidInput == null) return;

		// 匹配配方
		var inputResource = fluidInput.getResource(0);
		if (inputResource.isEmpty()) return;
		var inputId = BuiltInRegistries.FLUID.getKey(inputResource.getFluid());
		if (!(level instanceof ServerLevel serverLevel)) return;
		var optional = serverLevel.recipeAccess().getRecipeFor(ModRecipeTypes.PRESSURE_RELIEF_VALVE.get(), new PressureReliefValveRecipies.Input(inputId), level);
		if (optional.isEmpty()) return;
		var recipe = optional.get().value();

		// 换配方重置未产出魔力
		if (!recipe.inputFluid().equals(entity.recipeInputId)) {
			entity.unoutputtedMana = 0d;
			entity.recipeInputId = recipe.inputFluid();
		}

		double inputPerUnit = recipe.inputAmount();
		double heatPerUnit = recipe.heatPerLiter() * inputPerUnit;

		// 预期功率决定流体消耗速率, 实际功率决定产魔速率
		double expectedPower = 1e6 * 5 * (Math.pow(entity.powerFact + 0.5, 2) - 0.25);
		double actualPower = 1e6 * entity.powerFact;

		// 效率系数
		double eff = expectedPower > 0 ? actualPower / expectedPower : 0d;

		// 从流体存量计算潜在魔力(泄压阀无输出, 只受输入限制)
		double inputFluidAmount = fluidInput.getAmountAsInt(0);
		double hiddenManaFromInput = inputFluidAmount * heatPerUnit / inputPerUnit;

		// 可用魔力 = 流体存量限制 + 上轮累积未产出
		double hiddenMana = hiddenManaFromInput + entity.unoutputtedMana;

		// 本 tick 消耗的原始魔力(上限为预期功率)
		double consumedRawMana = Math.min(hiddenMana, expectedPower / 20);

		// 消耗配方数量的流体
		double recipeConsumeMult = Math.ceil((consumedRawMana - entity.unoutputtedMana) / heatPerUnit);

		if (recipeConsumeMult > 0) {
			try (var transaction = Transaction.openRoot()) {
				int consumeInput = (int) (recipeConsumeMult * inputPerUnit);
				if (fluidInput.extract(0, inputResource, consumeInput, transaction) != consumeInput) return;
				transaction.commit();
			}
		}

		// 按实际效率产魔
		networkData.setLoadW(new Mana(0d, 0d, 0d, -consumedRawMana * 20 * eff));
		entity.manaPowerW = consumedRawMana * 20 * eff;

		// 剩余未产出魔力留到下 tick
		entity.unoutputtedMana -= consumedRawMana - heatPerUnit * recipeConsumeMult;

		entity.setChanged();
	}
}
