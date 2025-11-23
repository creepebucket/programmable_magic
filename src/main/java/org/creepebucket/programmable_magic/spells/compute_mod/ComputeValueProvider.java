package org.creepebucket.programmable_magic.spells.compute_mod;

/**
 * 表示该 compute_mod 法术能够在上一次运行后提供一个值。
 */
public interface ComputeValueProvider {
    ComputeValue getProvidedValue();
}
