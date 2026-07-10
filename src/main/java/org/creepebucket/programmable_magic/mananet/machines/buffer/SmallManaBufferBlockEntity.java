package org.creepebucket.programmable_magic.mananet.machines.buffer;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;
import org.creepebucket.programmable_magic.utils.Mana;

public class SmallManaBufferBlockEntity extends NetNodeBlockEntity implements GeoBlockEntity {
	public AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

	public SmallManaBufferBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.SMALL_MANA_BUFFER_BLOCK_ENTITY.get(), pos, state);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return geoCache;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, SmallManaBufferBlockEntity entity) {
		if (level.isClientSide()) return;

		entity.getNetworkData().setCache(new Mana(1e9, 1e9, 1e9, 1e9));
	}
}
