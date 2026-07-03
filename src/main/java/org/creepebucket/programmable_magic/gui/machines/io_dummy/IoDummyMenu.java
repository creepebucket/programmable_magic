package org.creepebucket.programmable_magic.gui.machines.io_dummy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.creepebucket.programmable_magic.gui.lib.api.DynamicValue;
import org.creepebucket.programmable_magic.gui.lib.api.SyncMode;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.creepebucket.programmable_magic.gui.machines.api.MachineMenu;
import org.creepebucket.programmable_magic.mananet.machines.DummyBlockEntities;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;

public class IoDummyMenu extends MachineMenu {
	public DynamicValue<String> ioType;
	public DynamicValue<String> fluidId;
	public DynamicValue<Integer> fluidAmount;
	public DynamicValue<Integer> fluidCapacity;

	public Container itemContainer, fluidIoContainer;

	public IoDummyMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
		this(containerId, playerInv, extra.readBlockPos());
	}

	public IoDummyMenu(int containerId, Inventory playerInv, BlockPos pos) {
		super(ModMenuTypes.IO_DUMMY.get(), containerId, playerInv, InteractionHand.MAIN_HAND, m -> {
			IoDummyMenu menu = (IoDummyMenu) m;
			menu.pos = pos;
			menu.count = 15;
			menu.init();
		});
		for (int i = 0; i < 16; i++)
			addSlot(new Slot(itemContainer, i, -99, -99));

		// 流体输入/输出
		addSlot(new Slot(fluidIoContainer, 0, -99, -99));
		addSlot(new Slot(fluidIoContainer, 1, -99, -99));
	}

	public IoDummyMenu(int containerId, Inventory playerInv) {
		this(containerId, playerInv, InteractionHand.MAIN_HAND);
	}

	public IoDummyMenu(int containerId, Inventory playerInv, InteractionHand hand) {
		super(ModMenuTypes.IO_DUMMY.get(), containerId, playerInv, hand, Menu::init);
		for (int i = 0; i < 16; i++)
			addSlot(new Slot(itemContainer, i, -99, -99));

		// 流体输入/输出
		addSlot(new Slot(fluidIoContainer, 0, -99, -99));
		addSlot(new Slot(fluidIoContainer, 1, -99, -99));
	}

	@Override
	public void init() {
		initNetworkData();
		ioType = registerData("io_type", SyncMode.S2C, "");
		fluidId = registerData("fluid_id", SyncMode.S2C, "");
		fluidAmount = registerData("fluid_amount", SyncMode.S2C, 0);
		fluidCapacity = registerData("fluid_capacity", SyncMode.S2C, 0);

		itemContainer = new SimpleContainer(16);
		fluidIoContainer = new SimpleContainer(2);
		if (playerInv.player.level().isClientSide() || pos == null)
			return;

		BlockEntity be = playerInv.player.level().getBlockEntity(pos);
		if (be instanceof DummyBlockEntities.ItemInput ii) {
			ioType.set("item_input");
			itemContainer = ii.container;
		} else if (be instanceof DummyBlockEntities.ItemOutput io) {
			ioType.set("item_output");
			itemContainer = io.container;
		} else if (be instanceof DummyBlockEntities.FluidInput fi) {
			ioType.set("fluid_input");
			fluidIoContainer = fi.fluidIoContainer;
			FluidResource resource = fi.fluidHandler.getResource(0);
			fluidId.set(resource.isEmpty() ? "" : BuiltInRegistries.FLUID.getKey(resource.getFluid()).toString());
			fluidAmount.set(fi.fluidHandler.getAmountAsInt(0));
			fluidCapacity.set(fi.fluidHandler.getCapacityAsInt(0, FluidResource.EMPTY));
		} else if (be instanceof DummyBlockEntities.FluidOutput fo) {
			ioType.set("fluid_output");
			fluidIoContainer = fo.fluidIoContainer;
			FluidResource resource = fo.fluidHandler.getResource(0);
			fluidId.set(resource.isEmpty() ? "" : BuiltInRegistries.FLUID.getKey(resource.getFluid()).toString());
			fluidAmount.set(fo.fluidHandler.getAmountAsInt(0));
			fluidCapacity.set(fo.fluidHandler.getCapacityAsInt(0, FluidResource.EMPTY));
		}
	}

	@Override
	public void tick() {
		if (playerInv.player.level().isClientSide()) return;
		if (pos == null) return;
		if (count == 15) {
			count = 0;
			onNetworkSynced();
			BlockEntity be = playerInv.player.level().getBlockEntity(pos);
			if (be instanceof DummyBlockEntities.FluidInput fi)
				tryProcessFluidIo(fi.fluidIoContainer, fi.fluidHandler);
			else if (be instanceof DummyBlockEntities.FluidOutput fo)
				tryProcessFluidIo(fo.fluidIoContainer, fo.fluidHandler);
		}
		count++;
	}

	public void tryProcessFluidIo(Container io, ResourceHandler<FluidResource> handler) {
		ItemStack stack = io.getItem(0);
		if (stack.isEmpty() || !io.getItem(1).isEmpty()) return;
		ItemStacksResourceHandler temp = new ItemStacksResourceHandler(1);
		temp.set(0, ItemResource.of(stack), 1);
		ItemAccess access = ItemAccess.forHandlerIndex(temp, 0);
		ResourceHandler<FluidResource> itemFluid = access.getCapability(Capabilities.Fluid.ITEM);
		if (itemFluid == null) return;
		var moved = ResourceHandlerUtil.moveFirst(itemFluid, handler, fr -> true, Integer.MAX_VALUE, null);
		if (moved == null)
			moved = ResourceHandlerUtil.moveFirst(handler, itemFluid, fr -> true, Integer.MAX_VALUE, null);
		if (moved == null) return;
		io.getItem(0).shrink(1);
		io.setItem(1, temp.getResource(0).toStack());
		BlockEntity be = playerInv.player.level().getBlockEntity(pos);
		be.setChanged();
	}

	@Override
	protected void onNetworkSynced() {
		BlockEntity be = playerInv.player.level().getBlockEntity(pos);
		if (be instanceof DummyBlockEntities.FluidInput fi) {
			FluidResource resource = fi.fluidHandler.getResource(0);
			fluidId.set(resource.isEmpty() ? "" : BuiltInRegistries.FLUID.getKey(resource.getFluid()).toString());
			fluidAmount.set(fi.fluidHandler.getAmountAsInt(0));
		} else if (be instanceof DummyBlockEntities.FluidOutput fo) {
			FluidResource resource = fo.fluidHandler.getResource(0);
			fluidId.set(resource.isEmpty() ? "" : BuiltInRegistries.FLUID.getKey(resource.getFluid()).toString());
			fluidAmount.set(fo.fluidHandler.getAmountAsInt(0));
		}
	}
}
