package org.creepebucket.programmable_magic.mananet.machines.consumer.liquid_heater;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.mananet.machines.MachineBlockEntity;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;

public class LiquidHeaterBlockEntity extends MachineBlockEntity implements GeoBlockEntity {

	public final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

	public LiquidHeaterBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.LIQUID_HEATER_BLOCK_ENTITY.get(), pos, state);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return geoCache;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, LiquidHeaterBlockEntity entity) {
	}
}