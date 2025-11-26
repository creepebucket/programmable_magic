package org.creepebucket.programmable_magic.entities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
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
import org.creepebucket.programmable_magic.registries.ModEntityTypes;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.adjust_mod.BaseAdjustModLogic;
import org.creepebucket.programmable_magic.spells.base_spell.BaseBaseSpellLogic;
import org.creepebucket.programmable_magic.spells.compute_mod.ValueLiteralSpell;
import org.creepebucket.programmable_magic.spells.control_mod.BaseControlModLogic;
import static org.creepebucket.programmable_magic.ModUtils.sendErrorMessageToPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpellEntity extends Entity {
    private static final Logger LOGGER = LoggerFactory.getLogger("ProgrammableMagic:SpellEntity");
    private static final EntityDataAccessor<Boolean> DATA_ACTIVE = 
            SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.BOOLEAN);
    
    private SpellSequence spellSequence = new SpellSequence();
    private SpellData spellData;
    private SpellItemLogic currentSpell;
    private int delayTicks = 0;
    private Player caster;
    
    public SpellEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }
    
    public SpellEntity(Level level, Player caster, SpellSequence spellSequence) {
        this(ModEntityTypes.SPELL_ENTITY.get(), level);
        this.spellSequence = spellSequence;
        this.caster = caster;
        this.currentSpell = spellSequence.getFirstSpell();
        this.setPos(caster.getX(), caster.getEyeY(), caster.getZ());
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ACTIVE, true);
    }

    @Override
    public void tick() {
        super.tick();
        // 执行法术序列逻辑

        // 先检查法术执行完了吗
        if (currentSpell == null) { this.discard(); return; }

        // 再判断延迟
        if (delayTicks > 0) { delayTicks--; return; }

        // 使用 SpellUtils 执行当前法术一步
        var step = org.creepebucket.programmable_magic.spells.SpellUtils.executeCurrentSpell(
                caster, spellData, spellSequence, currentSpell);

        if (step.shouldDiscard) { this.discard(); return; }

        // 如果法术的 canExecute 判断不成立, 执行下一个法术
        if (!currentSpell.canExecute(caster, spellData)) { currentSpell = currentSpell.getNextSpell(); this.tick(); return; }

        // 如果法术设置了延时, 则设置
        if (step.delayTicks > 0) { delayTicks = step.delayTicks; return; }

        // 如果法术执行成功，则进入下一法术
        if (step.successful) {
            currentSpell = currentSpell.getNextSpell();
            this.tick();
            return;
        }

        if (!this.isNoGravity()) this.setNoGravity(true);
        if (this.level().isClientSide) { spawnParticles(); return; }
    }

    private static SpellItemLogic getAtIndex(SpellSequence seq, int idx) {
        int i = 0;
        for (SpellItemLogic it = seq.getFirstSpell(); it != null; it = it.getNextSpell()) {
            if (i == idx) return it;
            i++;
        }
        return null;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float v) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
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
