package org.creepebucket.programmable_magic.entities;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.registries.ModEntityTypes;
import org.creepebucket.programmable_magic.spells.*;
import org.creepebucket.programmable_magic.spells.compute_mod.ParenSpell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpellEntity extends Entity {
    private static final Logger LOGGER = LoggerFactory.getLogger("ProgrammableMagic:SpellEntity");
    private static final EntityDataAccessor<Boolean> DATA_ACTIVE = 
            SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.BOOLEAN);
    
    private SpellSequence spellSequence = new SpellSequence();
    private SpellData spellData;
    private SpellItemLogic currentSpell;
    // 记录上一个已执行完成的“边界”法术，用于动态截取待折叠的表达式区间
    private SpellItemLogic lastBoundarySpell = spellSequence.getFirstSpell();
    private int delayTicks = 0;
    private Player caster;
    private ModUtils.Mana mana;
    private List<ItemStack> pluginItems = List.of();
    
    public SpellEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }
    
    public SpellEntity(Level level, Player caster, SpellSequence spellSequence, SpellData spellData, ModUtils.Mana mana) {
        this(ModEntityTypes.SPELL_ENTITY.get(), level);
        this.spellSequence = spellSequence;
        this.caster = caster;
        this.spellData = spellData;
        this.spellData.setCustomData("spell_entity", this);
        this.currentSpell = spellSequence.getFirstSpell();
        this.lastBoundarySpell = spellSequence.getFirstSpell();
        this.setPos(caster.getX(), caster.getEyeY(), caster.getZ());
        this.mana = mana;
    }

    public SpellEntity(Level level, Player caster, SpellSequence spellSequence, SpellData spellData, ModUtils.Mana mana,
                       List<ItemStack> plugins) {
        this(level, caster, spellSequence, spellData, mana);
        if (plugins != null) this.pluginItems = plugins;
    }

    public List<ItemStack> getPluginItems() { return pluginItems; }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ACTIVE, true);
    }

    @Override
    public void tick() {
        super.tick();
        // 执行法术序列逻辑

        if (this.level().isClientSide()) { spawnParticles(); return; }
        if (this.spellData != null && Boolean.TRUE.equals(this.spellData.getCustomData("spell_error", Boolean.class))) { this.discard(); return; }

        // 若已附加“弹丸附加”效果，则先进行严格命中检测并结算伤害（严格：沿速度向量扫掠）
        handleProjectileAttachHit();

        // 应用速度（直接更新位置，不做碰撞解析以便穿过方块）
        this.setPos(this.getX() + this.getDeltaMovement().x, this.getY() + this.getDeltaMovement().y, this.getZ() + this.getDeltaMovement().z);

        // 先判断延迟
        if (delayTicks > 0) { delayTicks--; return; }

        // 再检查法术执行完了吗
        if (currentSpell == null) {
            this.discard();
            return;
        }

        if (!currentSpell.isExecutable()) {

            currentSpell = currentSpell.getNextSpell();
            this.tick(); // 递归执行下一个法术
            return;
        }

        // 进入 modifier/base/控制法术之前，先把前段表达式折叠为字面量，确保参数一致
        simplifyPendingExpressions(currentSpell);
        lastBoundarySpell = currentSpell;

        // 更新SpellData
        spellData.setPosition(new Vec3(this.getX(), this.getY(), this.getZ()));

        // 使用 SpellUtils 执行当前法术一步（回调在 SpellUtils 内部处理）
        var step = SpellUtils.executeCurrentSpell(caster, spellData, spellSequence, currentSpell, mana);


        // 扣蓝
        mana.add(step.mana.negative());

        if (step.shouldDiscard) { this.discard(); return; }

        // 如果法术执行成功，则进入下一法术
        if (!step.successful) {
            return;
        }

        // 如果法术的 canExecute 判断不成立, 执行下一个法术
        // if (!currentSpell.canExecute(caster, spellData)) { currentSpell = currentSpell.getNextSpell(); this.tick(); return; }

        // 如果法术设置了延时, 则设置
        if (step.delayTicks > 0) { delayTicks = step.delayTicks; }

        currentSpell = currentSpell.getNextSpell();

        // 如果法术重置了当前法术指针，则设置指针
        if (step.result.containsKey("current_spell")) { currentSpell = (SpellItemLogic) step.result.get("current_spell"); }


        this.tick();
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float v) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        // 持久化蓝量（四系）
        double r = valueInput.getDoubleOr("mana_radiation", this.mana != null ? this.mana.getRadiation() : 0.0);
        double t = valueInput.getDoubleOr("mana_temperature", this.mana != null ? this.mana.getTemperature() : 0.0);
        double m = valueInput.getDoubleOr("mana_momentum", this.mana != null ? this.mana.getMomentum() : 0.0);
        double p = valueInput.getDoubleOr("mana_pressure", this.mana != null ? this.mana.getPressure() : 0.0);
        this.mana = new ModUtils.Mana(r, t, m, p);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        if (this.mana != null) {
            valueOutput.putDouble("mana_radiation", this.mana.getRadiation());
            valueOutput.putDouble("mana_temperature", this.mana.getTemperature());
            valueOutput.putDouble("mana_momentum", this.mana.getMomentum());
            valueOutput.putDouble("mana_pressure", this.mana.getPressure());
        }
    }
    
    private void spawnParticles() {
        // 生成附魔台粒子效果
        for (int i = 0; i < Math.floor(this.getDeltaMovement().length() * 3); i++) {
            randomParticles(ParticleTypes.END_ROD, 3, 50, (double) i / 3);
            randomParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, 3, 10, (double) i / 3);
        }
    }

    private void randomParticles(ParticleOptions options, double a, double b, double partialTick) {
        // 依据随机方向与实体速度方向的夹角调整速度（越接近实体速度方向，粒子自身速度越低），并叠加实体自身速度
        Vec3 v = this.getDeltaMovement();
        Vec3 vDir = v.normalize();
        Vec3 randDir = new Vec3(this.random.nextGaussian(), this.random.nextGaussian(), this.random.nextGaussian()).normalize();
        double dot = randDir.dot(vDir); // [-1, 1]
        double scale = Math.pow((1.0 - dot) / 2, a) / b; // 接近同向时趋近 0，反向时最大
        Vec3 pv = randDir.scale(scale);

        this.level().addParticle(
                options,
                this.getX() + this.getDeltaMovement().x * partialTick,
                this.getY() + this.getDeltaMovement().y * partialTick,
                this.getZ() + this.getDeltaMovement().z * partialTick,
                pv.x, pv.y, pv.z
        );
    }

    private void handleProjectileAttachHit() {
        if (this.level().isClientSide()) return;
        if (this.spellData == null) return;
        if (!this.spellData.hasCustomData("projectile_attach_active")) return;
        var caster = this.caster;

        // 前后位置（更新位置前，因此用当前 pos 与 pos + v）
        var v = this.getDeltaMovement();
        var start = this.position();
        var end = start.add(v);

        // 使用包围盒扫掠查找命中的最近实体
        net.minecraft.world.phys.AABB pathBox = this.getBoundingBox().expandTowards(v).inflate(0.25);
        java.util.List<net.minecraft.world.entity.Entity> list = this.level().getEntities(this, pathBox, e -> e instanceof net.minecraft.world.entity.LivingEntity && e != this && e != caster);
        net.minecraft.world.entity.LivingEntity closest = null;
        double bestDist = Double.MAX_VALUE;
        for (net.minecraft.world.entity.Entity e : list) {
            net.minecraft.world.phys.AABB box = e.getBoundingBox().inflate(0.1);
            var clip = box.clip(start, end);
            if (clip.isPresent()) {
                double d = start.distanceTo(clip.get());
                if (d < bestDist) { bestDist = d; closest = (net.minecraft.world.entity.LivingEntity) e; }
            }
        }

        if (closest != null) {
            // 相对速度
            double speed = this.getDeltaMovement().subtract(closest.getDeltaMovement()).length();
            float damage = (float) speed;
            closest.hurt(closest.damageSources().playerAttack(caster), damage);

            // 清除标记，避免多次命中重复结算
            this.spellData.clearCustomData("projectile_attach_active");
            this.spellData.clearCustomData("projectile_attach_item");
        }
    }
    
    public boolean isActive() {
        return this.entityData.get(DATA_ACTIVE);
    }
    
    public void setActive(boolean active) {
        this.entityData.set(DATA_ACTIVE, active);
    }

    // 将上一个边界后至当前边界前的表达式区间抽出并简化
    private void simplifyPendingExpressions(SpellItemLogic boundary) {
        SpellItemLogic start = lastBoundarySpell.getNextSpell();
        while (start instanceof ParenSpell.RightParenSpell) { lastBoundarySpell = start; start = start.getNextSpell(); }
        SpellItemLogic end = boundary.getPrevSpell();
        if (start == null || end == null || start == boundary) return; // 空区间或起点即边界，直接跳过
        SpellSequence slice = cloneRange(start, end);
        SpellSequence simplified = SpellUtils.calculateSpellSequence(caster, spellData, slice);
        if (this.spellData != null && Boolean.TRUE.equals(this.spellData.getCustomData("spell_error", Boolean.class))) { this.discard(); return; }
        spellSequence.replaceSection(start, end, simplified);
    }

    private SpellSequence cloneRange(SpellItemLogic start, SpellItemLogic end) {
        // 克隆当前区间，避免直接在原序列上递归求值造成链表错乱
        SpellSequence seq = new SpellSequence();
        SpellItemLogic cursor = start;
        while (cursor != null) {
            seq.addLast(cursor.clone());
            if (cursor == end) break;
            cursor = cursor.getNextSpell();
        }
        return seq;
    }
}
