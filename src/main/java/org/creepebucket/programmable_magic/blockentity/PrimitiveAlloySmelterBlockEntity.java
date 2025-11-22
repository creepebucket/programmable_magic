package org.creepebucket.programmable_magic.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 原始合金熔炼炉 方块实体
 * - 仅存放输入状态：是否已放入纯净红石粉、是否已放入金锭
 * - 不做复杂库存，遵循 KISS
 */
public class PrimitiveAlloySmelterBlockEntity extends BlockEntity {

    private boolean hasPureRedstone = false;
    private boolean hasGoldIngot = false;

    public PrimitiveAlloySmelterBlockEntity(BlockPos pos, BlockState state) {
        super(org.creepebucket.programmable_magic.registries.ModBlockEntities.PRIMITIVE_ALLOY_SMELTER_BE.get(), pos, state);
    }

    public boolean hasPureRedstone() {return hasPureRedstone;}
    public boolean hasGoldIngot() {return hasGoldIngot;}

    public boolean tryInsertPureRedstone() {
        if (hasPureRedstone) return false;
        hasPureRedstone = true;
        setChanged();
        return true;
    }

    public boolean tryInsertGoldIngot() {
        if (hasGoldIngot) return false;
        hasGoldIngot = true;
        setChanged();
        return true;
    }

    public boolean readyToBurn() {return hasPureRedstone && hasGoldIngot;}

    public void consumeInputs() {
        hasPureRedstone = false;
        hasGoldIngot = false;
        setChanged();
    }

    public boolean takePureRedstone() {
        if (!hasPureRedstone) return false;
        hasPureRedstone = false;
        setChanged();
        return true;
    }

    public boolean takeGoldIngot() {
        if (!hasGoldIngot) return false;
        hasGoldIngot = false;
        setChanged();
        return true;
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.hasPureRedstone = input.getBooleanOr("hasPureRedstone", this.hasPureRedstone);
        this.hasGoldIngot = input.getBooleanOr("hasGoldIngot", this.hasGoldIngot);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("hasPureRedstone", this.hasPureRedstone);
        output.putBoolean("hasGoldIngot", this.hasGoldIngot);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }
}
