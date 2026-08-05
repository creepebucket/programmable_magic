package org.creepebucket.programmable_magic.mananet.machines.generator.steam_turbine;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.mananet.machines.BasicMachine;
import org.creepebucket.programmable_magic.mananet.machines.MachineBlockEntity;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;
import org.creepebucket.programmable_magic.registries.ModRecipeTypes;
import org.creepebucket.programmable_magic.utils.Mana;
import org.creepebucket.programmable_magic.utils.RelativeBlockPos;

public class SteamTurbineBlockEntity extends MachineBlockEntity implements GeoBlockEntity {

	public double powerFact = 1d;
	public boolean voidOverflow;
	public double unoutputtedMana;
	public double manaPowerW;
	public String recipeInputId = "";

	public final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

	public SteamTurbineBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.STEAM_TURBINE_BLOCK_ENTITY.get(), pos, state);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		powerFact = input.getDoubleOr("power_fact", 1d);
		voidOverflow = input.getBooleanOr("void_overflow", false);
		unoutputtedMana = input.getDoubleOr("unoutputted_mana", 0d);
		recipeInputId = input.getStringOr("recipe_input_id", "");
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putDouble("power_fact", powerFact);
		output.putBoolean("void_overflow", voidOverflow);
		output.putDouble("unoutputted_mana", unoutputtedMana);
		output.putString("recipe_input_id", recipeInputId);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return geoCache;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, SteamTurbineBlockEntity entity) {
		if (level.isClientSide()) return;

		// 连接到魔力网络
		var facing = level.getBlockState(pos).getValue(BasicMachine.FACING).getOpposite();
		var pending = pos.relative(facing).above();
		if (level.getBlockEntity(pending) instanceof NetNodeBlockEntity) {
			entity.connect(level, pending, Direction.DOWN, Direction.DOWN);
		}

		// 设置缓存上限
		var networkData = entity.getNetworkData();
		networkData.setCache(new Mana(2000d, 2000d, 200000000d, 2000d));

		entity.manaPowerW = 0;

		if (!entity.enabled) return;

		// 获取流体IO
		var block = (BasicMachine) entity.getBlockState().getBlock();
		RelativeBlockPos fluidInputPos = null, fluidOutputPos = null;
		for (var entry : block.IO_DEFINITION.entrySet()) {
			switch (entry.getValue()) {
				case "fluid_input" -> fluidInputPos = entry.getKey();
				case "fluid_output" -> fluidOutputPos = entry.getKey();
			}
		}
		var fluidInput = fluidInputPos != null ? entity.fluidStorage.get(fluidInputPos) : null;
		var fluidOutput = fluidOutputPos != null ? entity.fluidStorage.get(fluidOutputPos) : null;
		if (fluidInput == null) return;

		// 匹配配方
		var inputResource = fluidInput.getResource(0);
		if (inputResource.isEmpty()) return;
		var inputId = BuiltInRegistries.FLUID.getKey(inputResource.getFluid());
		if (!(level instanceof ServerLevel serverLevel)) return;
		var optional = serverLevel.recipeAccess().getRecipeFor(ModRecipeTypes.STEAM_TURBINE.get(), new SteamTurbineRecipies.Input(inputId), level);
		if (optional.isEmpty()) return;
		var recipe = optional.get().value();

		// 换配方重置未产出魔力
		if (!recipe.inputFluid().equals(entity.recipeInputId)) {
			entity.unoutputtedMana = 0d;
			entity.recipeInputId = recipe.inputFluid();
		}

		double inputPerUnit = recipe.inputAmount();
		double outputPerUnit = recipe.outputAmount();
		double heatPerUnit = recipe.heatPerLiter() * inputPerUnit;

		// 预期功率决定流体消耗速率, 实际功率决定产魔速率
		double expectedPower = 1e6 * 5 * (Math.pow(entity.powerFact + 0.5, 2) - 0.25);
		double actualPower = 1e6 * entity.powerFact;

		// 效率系数
		double eff = expectedPower > 0 ? actualPower / expectedPower : 0d;

		// 从流体存量计算潜在魔力
		double inputFluidAmount = fluidInput.getAmountAsInt(0);
		double outputFluidAmount = fluidOutput != null ? fluidOutput.getAmountAsInt(0) : 0;

		double hiddenManaFromInput = inputFluidAmount * heatPerUnit / inputPerUnit;
		double hiddenManaFromOutput = entity.voidOverflow ? Double.MAX_VALUE : (16000 - outputFluidAmount) * heatPerUnit / outputPerUnit;

		// 可用魔力 = 流体存量限制 + 上轮累积未产出
		double hiddenMana = Math.min(hiddenManaFromInput, hiddenManaFromOutput) + entity.unoutputtedMana;

		// 本 tick 消耗的原始魔力(上限为预期功率)
		double consumedRawMana = Math.min(hiddenMana, expectedPower / 20);

		// 消耗配方数量的流体
		double recipeConsumeMult = Math.ceil((consumedRawMana - entity.unoutputtedMana) / heatPerUnit);

		if (recipeConsumeMult > 0) {
			var outputResource = FluidResource.of(BuiltInRegistries.FLUID.getValue(Identifier.parse(recipe.outputFluid())));
			try (var transaction = Transaction.openRoot()) {
				int consumeInput = (int) (recipeConsumeMult * inputPerUnit);
				if (fluidInput.extract(0, inputResource, consumeInput, transaction) != consumeInput) return;
				if (fluidOutput != null) {
					int consumeOutput = (int) (recipeConsumeMult * outputPerUnit);
					if (!entity.voidOverflow && fluidOutput.insert(0, outputResource, consumeOutput, transaction) != consumeOutput) return;
					if (entity.voidOverflow) fluidOutput.insert(0, outputResource, consumeOutput, transaction);
				}
				transaction.commit();
			}
		}

		// 按实际效率产魔
		networkData.setLoadW(new Mana(0d, 0d, -consumedRawMana * 20 * eff, 0d));
		entity.manaPowerW = consumedRawMana * 20 * eff;

		// 剩余未产出魔力留到下 tick
		entity.unoutputtedMana -= consumedRawMana - heatPerUnit * recipeConsumeMult;

		entity.setChanged();
	}
}
