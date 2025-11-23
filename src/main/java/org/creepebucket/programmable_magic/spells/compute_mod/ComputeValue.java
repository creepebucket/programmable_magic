package org.creepebucket.programmable_magic.spells.compute_mod;

import org.creepebucket.programmable_magic.spells.SpellValueType;

/**
 * compute_mod 节点运算后的通用值包装。
 */
public record ComputeValue(SpellValueType type, Object value) {
}
