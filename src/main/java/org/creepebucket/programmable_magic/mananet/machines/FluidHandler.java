package org.creepebucket.programmable_magic.mananet.machines;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class FluidHandler extends SnapshotJournal<FluidStack> implements ResourceHandler<FluidResource> {

	public FluidStack tank = FluidStack.EMPTY;
	public long capacity;
	public Runnable onChanged = () -> {};

	public FluidHandler(long capacity) {
		this.capacity = capacity;
	}

	@Override
	protected void onRootCommit(FluidStack originalState) {
		onChanged.run();
	}

	@Override
	protected FluidStack createSnapshot() {
		return tank.copy();
	}

	@Override
	protected void revertToSnapshot(FluidStack snapshot) {
		tank = snapshot;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public FluidResource getResource(int index) {
		return FluidResource.of(tank);
	}

	@Override
	public long getAmountAsLong(int index) {
		return tank.getAmount();
	}

	@Override
	public long getCapacityAsLong(int index, FluidResource resource) {
		return capacity;
	}

	@Override
	public boolean isValid(int index, FluidResource resource) {
		return tank.isEmpty() || FluidStack.isSameFluidSameComponents(tank, resource.toStack(1));
	}

	@Override
	public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
		TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
		updateSnapshots(transaction);
		if (!tank.isEmpty() && !FluidStack.isSameFluidSameComponents(tank, resource.toStack(1))) return 0;
		long space = capacity - tank.getAmount();
		int inserted = (int) Math.min(amount, space);
		if (inserted <= 0) return 0;
		if (tank.isEmpty()) {
			tank = resource.toStack(inserted);
		} else {
			tank.grow(inserted);
		}
		return inserted;
	}

	@Override
	public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
		TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
		updateSnapshots(transaction);
		if (tank.isEmpty()) return 0;
		if (!FluidStack.isSameFluidSameComponents(tank, resource.toStack(1))) return 0;
		int extracted = (int) Math.min(amount, tank.getAmount());
		tank.shrink(extracted);
		if (tank.isEmpty()) tank = FluidStack.EMPTY;
		return extracted;
	}
}
