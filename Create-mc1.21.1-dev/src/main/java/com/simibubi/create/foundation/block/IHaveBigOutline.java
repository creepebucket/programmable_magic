package com.simibubi.create.foundation.block;

import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.simibubi.create.content.trains.track.TrackBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Implementing this interface will allow you to have bigger outlines when overriding {@link BlockBehaviour#getInteractionShape(BlockState, BlockGetter, BlockPos)}
 * <p>
 * For examples look at {@link TrackBlock} and {@link SlidingDoorBlock}
 */
public interface IHaveBigOutline { }
