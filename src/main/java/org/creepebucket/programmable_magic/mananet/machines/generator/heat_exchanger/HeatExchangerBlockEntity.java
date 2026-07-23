package org.creepebucket.programmable_magic.mananet.machines.generator.heat_exchanger;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.creepebucket.programmable_magic.mananet.machines.MachineBlockEntity;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;
import org.creepebucket.programmable_magic.utils.Mana;

public class HeatExchangerBlockEntity extends MachineBlockEntity implements GeoBlockEntity {

	public double power;
	public double powerFact = 1d;

	public final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

	public HeatExchangerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.HEAT_EXCHANGER_BLOCK_ENTITY.get(), pos, state);
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

	public static void tick(Level level, BlockPos pos, BlockState state, HeatExchangerBlockEntity entity) {
		if (level.isClientSide()) return;

		var networkData = entity.getNetworkData();
		networkData.setCache(new Mana(2000d, 2000d, 2000d, 2000d));
	}
}
