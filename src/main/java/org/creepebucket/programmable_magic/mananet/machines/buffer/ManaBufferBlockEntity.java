package org.creepebucket.programmable_magic.mananet.machines.buffer;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.mananet.machines.MachineBlockEntity;
import org.creepebucket.programmable_magic.utils.Mana;

public class ManaBufferBlockEntity extends MachineBlockEntity implements GeoBlockEntity {
	public AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

	public double baseStorage, baseExpansion, baseExpansionPower, maxChargePower;
	public int chargeSlotCount;
	public double powerFact = 1d;
	public int connectHeight;

	public ManaBufferBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return geoCache;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, ManaBufferBlockEntity entity) {
		if (level.isClientSide()) return;

		entity.getNetworkData().setCache(new Mana(entity.baseStorage, entity.baseStorage, entity.baseStorage, entity.baseStorage));

		if (level.getBlockEntity(pos.above(entity.connectHeight)) instanceof NetNodeBlockEntity nodeBe) {
			nodeBe.connect(level, pos, Direction.UP, Direction.DOWN);
		}
	}
}
