package org.creepebucket.programmable_magic.spells;

import java.util.List;

/**
 * 简单的法术双向链表
 */
public class SpellSequence {

    private SpellItemLogic head;
    private SpellItemLogic tail;
    private int size;

    public SpellSequence() {}

    public SpellSequence(List<SpellItemLogic> list) {
        if (list == null || list.isEmpty()) return;
        for (SpellItemLogic s : list) addLast(s);
    }

    // 基本方法
    public SpellItemLogic getFirstSpell() { return head; }
    public SpellItemLogic getLastSpell() { return tail; }
    public int size() { return size; }
    public boolean isEmpty() { return head == null; }

    public void addLast(SpellItemLogic s) {
        if (s == null) return;
        if (head == null) {
            head = tail = s;
            s._setPrev(null);
            s._setNext(null);
        } else {
            tail._setNext(s);
            s._setPrev(tail);
            s._setNext(null);
            tail = s;
        }
        size++;
    }

    /**
     * 将当前序列中 [L..R]（含端点）替换为另一个序列 section。
     */
    public boolean replaceSection(SpellItemLogic L, SpellItemLogic R, SpellSequence section) {
        // 计算待移除片段长度，且验证 L 能到达 R
        int removeCount = 0;
        SpellItemLogic it = L;
        while (true) {
            removeCount++;
            if (it == R) break;
            it = it.getNextSpell();
            if (it == null) return false; // L 到不了 R
        }

        SpellItemLogic prev = L.getPrevSpell();
        SpellItemLogic next = R.getNextSpell();

        // 接入新片段
        SpellItemLogic newHead = (section == null) ? null : section.head;
        SpellItemLogic newTail = (section == null) ? null : section.tail;

        if (newHead != null) newHead._setPrev(prev);
        if (prev != null) prev._setNext(newHead);
        // 更新全局头
        if (head == L) head = (newHead != null) ? newHead : next;

        if (newTail != null) newTail._setNext(next);
        if (next != null) next._setPrev(newTail);
        // 更新全局尾
        if (tail == R) tail = (newTail != null) ? newTail : prev;

        // 更新尺寸
        int addCount = (section == null) ? 0 : section.size;
        size = size - removeCount + addCount;
        return true;
    }
}
