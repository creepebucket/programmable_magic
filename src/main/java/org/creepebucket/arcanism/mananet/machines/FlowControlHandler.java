package org.creepebucket.arcanism.mananet.machines;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

// 处理仅输入/输出
public class FlowControlHandler<T extends Resource> implements ResourceHandler<T> {
    // 包装器, 只阻断输入/输出, 以及一些其他工具方法

    public ResourceHandler<T> handler;
    public boolean canInput, canOutput;

    public FlowControlHandler(ResourceHandler<T> handler, boolean canInput, boolean canOutput) {
        this.handler = handler;

        this.canInput = canInput;
        this.canOutput = canOutput;
    }

    @Override
    public int size() {
        return handler.size();
    }

    @Override
    public T getResource(int index) {
        return handler.getResource(index);
    }

    @Override
    public long getAmountAsLong(int index) {
        return handler.getAmountAsLong(index);
    }

    @Override
    public int getAmountAsInt(int index) {
        return handler.getAmountAsInt(index);
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        return handler.getCapacityAsLong(index, resource);
    }

    @Override
    public int getCapacityAsInt(int index, T resource) {
        return handler.getCapacityAsInt(index, resource);
    }

    @Override
    public boolean isValid(int index, T resource) {
        return handler.isValid(index, resource);
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        return canInput ? handler.insert(index, resource, amount, transaction) : 0;
    }

	@Override
	public int insert(T resource, int amount, TransactionContext transaction) {
		return canInput ? handler.insert(resource, amount, transaction) : 0;
	}

	@Override
	public int extract(int index, T resource, int amount, TransactionContext transaction) {
		return canOutput ? handler.extract(index, resource, amount, transaction) : 0;
	}

	@Override
	public int extract(T resource, int amount, TransactionContext transaction) {
		return canOutput ? handler.extract(resource, amount, transaction) : 0;
	}
}
