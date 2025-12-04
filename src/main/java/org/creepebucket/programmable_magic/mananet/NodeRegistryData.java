package org.creepebucket.programmable_magic.mananet;

/**
 * 节点注册与资源元数据。
 * - name：注册名（唯一，作方块/方块实体/物品的 id）。
 * - titleKey：显示名称翻译键（可选，留空则不处理）。
 * - modelPath/texturePath：供数据生成或资源管线使用（当前不自动生成，仅存档）。
 * - withBlockItem：是否注册方块物品。
 */
public record NodeRegistryData(
        String name,
        String titleKey,
        String modelPath,
        String texturePath,
        boolean withBlockItem
) {}

