package org.creepebucket.programmable_magic.spells.api;

/*
 * 法术链表实现
 */
public class SpellSequence {

    // 约定：空表时 head==null 且 tail==null 且 size==0；非空时 head/tail 始终指向两端

    // 头和尾
    public SpellItemLogic head;
    public SpellItemLogic tail;

    /*
     * 从左侧推入一个法术
     */
    public SpellSequence pushLeft(SpellItemLogic spell) {
        // 法术入表
        spell.prev = null;
        spell.next = head;

        // 更新头
        if (head != null) head.prev = spell;
        head = spell;
        if (tail == null) tail = spell;

        return this;
    }

    /*
     * 从右侧推入一个法术
     */
    public SpellSequence pushRight(SpellItemLogic spell) {
        // 法术入表
        spell.next = null;
        spell.prev = tail;

        // 更新尾
        if (tail != null) tail.next = spell;
        tail = spell;
        if (head == null) head = spell;

        return this;
    }

    /*
     * 从左侧弹出一个法术
     */
    public SpellItemLogic popLeft() {
        // 检查是否有头
        if (head == null) throw new RuntimeException("试图对空法术列表进行popLeft操作");

        // 更新头
        SpellItemLogic newHead = head.next;
        SpellItemLogic originalHead = head;

        if (newHead == null) {
            // 链表只有一个元素
            head = null;
            tail = null;
        } else {
            head = newHead;
            newHead.prev = null;
        }
        return originalHead;
    }

    /*
     * 从右侧弹出一个法术
     */
    public SpellItemLogic popRight() {
        // 检查是否有尾
        if (tail == null) throw new RuntimeException("试图对空法术列表进行popRight操作");

        // 更新尾
        SpellItemLogic newTail = tail.prev;
        SpellItemLogic originalTail = tail;

        if (newTail == null) {
            // 链表只有一个元素
            head = null;
            tail = null;
        } else {
            tail = newTail;
            newTail.next = null;
        }
        return originalTail;
    }

    /*
     * 从左侧推入一个链表
     */
    public void pushLeft(SpellSequence list) {
        // 检查被推链表是否为空
        if (list.head == null) return;
        if (head == null) {
            // 当前为空表直接复制头尾
            head = list.head;
            tail = list.tail;
            return;
        }

        // 更新头尾
        list.tail.next = head;
        head.prev = list.tail;

        head = list.head;
    }

    /*
     * 从右侧推入一个链表
     */
    public void pushRight(SpellSequence list) {
        // 检查被推链表是否为空
        if (list.head == null) return;
        if (tail == null) {
            // 当前为空表直接复制头尾
            head = list.head;
            tail = list.tail;
            return;
        }

        // 更新头尾
        list.head.prev = tail;
        tail.next = list.head;
        tail = list.tail;
    }

    /*
     * 替换链表子序列
     * 给定SpellItemLogic L, R, 替换[L, R]之间的所有法术为新序列(L, R会被替换)
     */
    public void replaceSection(SpellItemLogic L, SpellItemLogic R, SpellSequence section) {
        // 更新头尾
        SpellItemLogic prev = L.prev;
        SpellItemLogic next = R.next;

        SpellItemLogic newHead = section == null ? null : section.head;
        SpellItemLogic newTail = section == null ? null : section.tail;

        if (newHead != null) {
            // section非空
            newHead.prev = prev;
            if (prev != null) prev.next = newHead;
            if (head == L) head = newHead;

            newTail.next = next;
            if (next != null) next.prev = newTail;
            if (tail == R) tail = newTail;
        } else {
            // section为空直接删除 [L..R]
            if (prev != null) prev.next = next;
            if (next != null) next.prev = prev;
            if (head == L) head = next;
            if (tail == R) tail = prev;
        }
    }

    /*
     * 从链表中截取一个子列表(被深拷贝)
     * 子列表包含L, R
     */
    public SpellSequence subSequence(SpellItemLogic L, SpellItemLogic R) {
        // 子列表
        SpellSequence seq = new SpellSequence();

        SpellItemLogic p = L;
        while (true) {
            // 依次入表
            seq.pushRight(p.clone());

            if (p == R) break;
            p = p.next;
            if (p == null) throw new RuntimeException("在对SpellSequence进行subSequence操作时, LR不连通");
        }

        return seq;
    }
}
