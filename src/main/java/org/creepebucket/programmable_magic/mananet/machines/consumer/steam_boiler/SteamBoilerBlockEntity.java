package org.creepebucket.programmable_magic.mananet.machines.consumer.steam_boiler;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.mananet.machines.MachineBlockEntity;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;

public class SteamBoilerBlockEntity extends MachineBlockEntity implements GeoBlockEntity {

	public final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

	public SteamBoilerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.STEAM_BOILER_BLOCK_ENTITY.get(), pos, state);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return geoCache;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, SteamBoilerBlockEntity entity) {
	}
}