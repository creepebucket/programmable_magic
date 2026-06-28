package org.creepebucket.programmable_magic.mananet.machines.consumer.water_pump;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;

public class WaterPumpBlockEntity extends NetNodeBlockEntity implements GeoBlockEntity {

	public double power;
	public double powerFact = 1d;

	public final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

	public WaterPumpBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.WATER_PUMP_BLOCK_ENTITY.get(), pos, state);
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

		if (!level.isClientSide() && level.getBlockEntity(pos.east(1)) instanceof NetNodeBlockEntity) {
			entity.connect(level, pos.east(1), Direction.WEST, Direction.EAST);
		}
		if (!level.isClientSide() && level.getBlockEntity(pos.west(1)) instanceof NetNodeBlockEntity) {
			entity.connect(level, pos.west(1), Direction.EAST, Direction.WEST);
		}
		if (!level.isClientSide() && level.getBlockEntity(pos.south(1)) instanceof NetNodeBlockEntity) {
			entity.connect(level, pos.south(1), Direction.NORTH, Direction.SOUTH);
		}
		if (!level.isClientSide() && level.getBlockEntity(pos.north(1)) instanceof NetNodeBlockEntity) {
			entity.connect(level, pos.north(1), Direction.SOUTH, Direction.NORTH);
		}

		var load = new ModUtils.Mana(0d, 0d, 0d, 0d);
		if (entity.getNetworkData().canProduce(load)) entity.getNetworkData().setLoad(load);
		entity.getNetworkData().setCache(new ModUtils.Mana(2d, 2d, 2d, 2d));
	}
}
