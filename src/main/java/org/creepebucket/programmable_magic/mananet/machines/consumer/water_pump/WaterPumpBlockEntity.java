package org.creepebucket.programmable_magic.mananet.machines.consumer.water_pump;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.mananet.machines.MachineBlockEntity;
import org.creepebucket.programmable_magic.mananet.machines.RotatableBasicMachine;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;

import static net.minecraft.world.level.block.Blocks.WATER;

public class WaterPumpBlockEntity extends MachineBlockEntity implements GeoBlockEntity {

	public double power;
	public double powerFact = 1d;

	public final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

	public WaterPumpBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.WATER_PUMP_BLOCK_ENTITY.get(), pos, state);
		fluidOutputHandler = createFluidHandler(64000);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		powerFact = input.getDoubleOr("power_fact", 1d);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putDouble("power_fact", powerFact);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return geoCache;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, WaterPumpBlockEntity entity) {
		if (level.isClientSide()) return;

		var pending = pos.relative(level.getBlockState(pos).getValue(RotatableBasicMachine.FACING).getOpposite()).above();
		if (level.getBlockEntity(pending) instanceof NetNodeBlockEntity) {
			entity.connect(level, pending, Direction.DOWN, Direction.DOWN);
		}

		var networkData = entity.getNetworkData();
		networkData.setCache(new ModUtils.Mana(2000d, 2000d, 2000d, 2000d));

		var load = new ModUtils.Mana(Math.pow(4, entity.powerFact - 1) * 300, 0d, 2000 * entity.powerFact, 0d);

		var shouldBeWater = level.getBlockState(pos.relative(level.getBlockState(pos).getValue(RotatableBasicMachine.FACING).getOpposite()).below());
		if (!networkData.canProduce(load) || !entity.enabled || !shouldBeWater.is(WATER)) {
			entity.power = 0;
			return;
		}
		networkData.setLoadW(load);
		entity.power = entity.powerFact * 1000;

		try (var tx = Transaction.openRoot()) {
			entity.fluidOutputHandler.insert(FluidResource.of(Fluids.WATER), (int) (50 * entity.powerFact), tx);
			tx.commit();
		}
	}
}
