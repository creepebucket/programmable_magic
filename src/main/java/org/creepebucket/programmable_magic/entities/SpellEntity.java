package org.creepebucket.programmable_magic.entities;

import com.fasterxml.jackson.databind.ser.Serializers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.registries.ModEntityTypes;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.base_spell.BaseSpellEffectLogic;
import org.creepebucket.programmable_magic.spells.compute_mod.ComputeRuntime;

import java.util.ArrayList;
import java.util.List;

public class SpellEntity extends Entity {
    private static final EntityDataAccessor<Boolean> DATA_ACTIVE = 
            SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.BOOLEAN);
    
    private List<SpellItemLogic> spellSequence = new ArrayList<>();
    private SpellData spellData;
    private int currentSpellIndex = 0;
    private int delayTicks = 0;
    private Player caster;
    
    public SpellEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }
    
    public SpellEntity(Level level, Player caster) {
        this(ModEntityTypes.SPELL_ENTITY.get(), level);
        this.caster = caster;
        this.setPos(caster.getX(), caster.getEyeY(), caster.getZ());
        
        // 设置初始速度
        Vec3 lookDirection = caster.getLookAngle();
        this.setDeltaMovement(lookDirection.scale(0.5));
        this.setNoGravity(true);
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ACTIVE, true);
    }
    
    public void setSpellSequence(List<SpellItemLogic> spellSequence, SpellData spellData) {
        this.spellSequence = new ArrayList<>(spellSequence);
        this.spellData = spellData;
        // 将自身作为载体赋给 SpellData，供载体相关法术使用
        if (this.spellData != null) {
            this.spellData.setTarget(this);
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 确保无重力
        if (!this.isNoGravity()) {
            this.setNoGravity(true);
        }
        
        if (this.level().isClientSide) {
            // 客户端粒子效果
            spawnParticles();
            return;
        }
        
        // 服务端逻辑
        if (!isActive() || spellSequence.isEmpty() || currentSpellIndex >= spellSequence.size()) {
            this.discard();
            return;
        }
        
        // 维持恒定速度（取消空气阻力导致的速度衰减）
        Vec3 velocity = this.getDeltaMovement();
        if (velocity.lengthSqr() > 0) {
            this.setDeltaMovement(velocity);
        }
        
        // 更新法术数据位置与载体
        if (spellData != null) {
            spellData.setPosition(this.position());
            spellData.setDirection(this.getDeltaMovement().normalize());
            spellData.setTarget(this);
        }
        
        // 如果处于延时，则仅递减延时，不执行法术，但仍然继续移动
        if (delayTicks > 0) {
            delayTicks--;
        } else {
            if (spellData != null) {
                while (currentSpellIndex < spellSequence.size()
                        && spellData.consumeSkippedIndex(currentSpellIndex)) {
                    currentSpellIndex++;
                }
            }
            if (currentSpellIndex >= spellSequence.size()) {
                this.discard();
                return;
            }

            // 执行当前法术
            SpellItemLogic currentSpell = spellSequence.get(currentSpellIndex);
            try {
                // 向 SpellData 注入执行上下文：当前序列与索引，供计算类法术读取
                if (spellData != null) {
                    spellData.setCustomData("__seq", this.spellSequence);
                    spellData.setCustomData("__idx", this.currentSpellIndex);
                }
                boolean shouldContinue = currentSpell.run(caster, spellData);
                ComputeRuntime.recordProvidedValue(spellData, currentSpell, this.currentSpellIndex);
                
                if (shouldContinue) {
                    currentSpellIndex++;
                } else {
                    // 法术要求下一tick再处理
                    delayTicks = 1;
                }
                
                // 检查法术数据中的延时
                if (spellData != null && spellData.getDelay() > 0) {
                    delayTicks = Math.max(delayTicks, spellData.getDelay());
                    spellData.setDelay(0); // 重置延时
                }

                // 如果法术是base, 重置威力
                if (currentSpell.getSpellType() == SpellItemLogic.SpellType.BASE_SPELL) {
                    spellData.setPower(1);
                }
                
            } catch (Exception e) {
                // 法术执行出错，终止法术
                this.discard();
            }
        }
        
        // 移动实体（不进行空气阻力减速）
        this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
        
        // 检查是否超出范围
        if (spellData != null && this.distanceToSqr(spellData.getCaster()) > spellData.getRange() * spellData.getRange()) {
            // this.discard(); 暂时不要
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float v) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.setActive(valueInput.getBooleanOr("Active", true));
        this.currentSpellIndex = valueInput.getIntOr("CurrentSpellIndex", 0);
        this.delayTicks = valueInput.getIntOr("DelayTicks", 0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        valueOutput.putBoolean("Active", this.isActive());
        valueOutput.putInt("CurrentSpellIndex", this.currentSpellIndex);
        valueOutput.putInt("DelayTicks", this.delayTicks);
    }
    
    private void spawnParticles() {
        // 生成附魔台粒子效果
        for (int i = 0; i < 3; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
            double offsetY = (this.random.nextDouble() - 0.5) * 0.5;
            double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;
            
            this.level().addParticle(
                    ParticleTypes.ENCHANT,
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    0.0, 0.05, 0.0
            );
        }
    }
    
    public boolean isActive() {
        return this.entityData.get(DATA_ACTIVE);
    }
    
    public void setActive(boolean active) {
        this.entityData.set(DATA_ACTIVE, active);
    }
    
} 
