package org.creepebucket.programmable_magic.spells.spells_base;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.api.ExecutionResult;
import org.creepebucket.programmable_magic.spells.api.SpellExceptions;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public abstract class EntityInteractionSpell extends SpellItemLogic implements SpellItemLogic.BaseSpell {

    public EntityInteractionSpell() {
        subCategory = "spell." + MODID + ".subcategory.entity";
        precedence = -99;
        bypassShunting = true;
    }

    @Override
    public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return true;
    }

    @Override
    public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return new ModUtils.Mana();
    }

    public static class TeleportSpell extends EntityInteractionSpell {
        public TeleportSpell() {
            name = "teleport";
            inputTypes = List.of(List.of(SpellValueType.VECTOR3, SpellValueType.ENTITY));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            Entity target = (Entity) paramsList.get(0);
            Vec3 delta = (Vec3) paramsList.get(1);
            target.teleportTo(target.getX() + delta.x, target.getY() + delta.y, target.getZ() + delta.z);
            return ExecutionResult.SUCCESS(this);
        }
    }

    public static class VelocitySpell extends EntityInteractionSpell {
        public VelocitySpell() {
            name = "gain_velocity";
            inputTypes = List.of(List.of(SpellValueType.VECTOR3, SpellValueType.ENTITY));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            Entity target = (Entity) paramsList.get(1);
            Vec3 delta = (Vec3) paramsList.get(0);
            target.addDeltaMovement(delta);
            target.hurtMarked = true;
            return ExecutionResult.SUCCESS(this);
        }
    }

    public static class ApplyPotionSpell extends EntityInteractionSpell {
        public ApplyPotionSpell() {
            name = "apply_potion";
            inputTypes = List.of(List.of(SpellValueType.ITEM, SpellValueType.ENTITY));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            Entity target = (Entity) paramsList.get(0);
            ItemStack potionStack = (ItemStack) paramsList.get(1);

            if (!(target instanceof LivingEntity living)) {
                SpellExceptions.INVALID_INPUT(this).throwIt(caster);
                return ExecutionResult.ERRORED();
            }

            PotionContents potionContents = potionStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            potionContents.applyToLivingEntity(living, potionStack.getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0F));
            return ExecutionResult.SUCCESS(this);
        }
    }

    public static class SendToInventorySpell extends EntityInteractionSpell {
        public SendToInventorySpell() {
            name = "send_to_inventory";
            inputTypes = List.of(List.of(SpellValueType.ITEM));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            ItemStack stack = (ItemStack) paramsList.get(0);
            caster.getInventory().placeItemBackInInventory(stack);
            return ExecutionResult.SUCCESS(this);
        }
    }
}
