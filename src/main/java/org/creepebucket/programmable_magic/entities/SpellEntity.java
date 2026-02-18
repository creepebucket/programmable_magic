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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.registries.ModEntityTypes;
import org.creepebucket.programmable_magic.spells.api.ExecutionResult;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class SpellEntity extends Entity {
    private static final Logger LOGGER = LoggerFactory.getLogger("ProgrammableMagic:SpellEntity");
    private static final EntityDataAccessor<Boolean> DATA_ACTIVE =
            SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.BOOLEAN);

    // 一些数据定义

    // 可用的魔力
    public ModUtils.Mana availableMana = new ModUtils.Mana();
    // 插件列表
    public List<ItemStack> pluginItems = List.of();
    // 法术列表
    public SpellSequence spellSequence = new SpellSequence();
    // 法术数据
    public Map<String, Object> spellData = Map.of();
    // 施法者
    public Player caster;
    // 正在释放的法术
    public SpellItemLogic currentSpell;
    // 延迟刻
    public int delayTicks = 0;
    // 原始法术列表
    public SpellSequence originalSpellSequence;

    public SpellEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public SpellEntity(Level level, Player caster, SpellSequence spellSequence, Map<String, Object> spellData, ModUtils.Mana mana, List<ItemStack> plugins) {
        // 创建实体
        this(ModEntityTypes.SPELL_ENTITY.get(), level);

        // 设置数据
        this.caster = caster;
        this.spellSequence = spellSequence;
        this.spellData = spellData;
        this.availableMana = mana;
        this.pluginItems = plugins;
        this.originalSpellSequence = spellSequence.subSequence(spellSequence.head, spellSequence.tail);

        this.setPos(caster.getX(), caster.getY(), caster.getZ());

        // 初始化法术指针
        this.currentSpell = spellSequence.head;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ACTIVE, true);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        // 检查延迟
        if (delayTicks > 0) {
            delayTicks--;
            return;
        }

        while (delayTicks <= 0 && currentSpell != null) {
            // 执行法术序列逻辑
            ExecutionResult result = currentSpell.runWithCheck(caster, spellSequence, this);
            // 设置延迟
            delayTicks = result.delayTicks;
            // 停止实体
            if (result.doStop) this.discard();
            // 设置下一个法术
            currentSpell = result.nextSpell;
        }

        if (currentSpell == null) this.discard();
    }

    // 暂时不支持持久化
    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float v) {
        return false;
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

    public boolean isActive() {
        return this.entityData.get(DATA_ACTIVE);
    }

    public void setActive(boolean active) {
        this.entityData.set(DATA_ACTIVE, active);
    }
}
