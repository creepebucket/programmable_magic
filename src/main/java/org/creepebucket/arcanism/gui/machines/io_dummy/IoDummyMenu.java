package org.creepebucket.arcanism.gui.machines.io_dummy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.creepebucket.arcanism.gui.lib.api.DynamicValue;
import org.creepebucket.arcanism.gui.lib.api.SyncMode;
import org.creepebucket.arcanism.gui.machines.api.MachineMenu;
import org.creepebucket.arcanism.registries.ModMenuTypes;

public class IoDummyMenu extends MachineMenu {
	public DynamicValue<String> ioType;
	public DynamicValue<String> fluidId;
	public DynamicValue<Integer> fluidAmount;
	public DynamicValue<Integer> fluidCapacity;
	public ResourceHandler<?> resourceHandler;
	public Container fluidIoContainer;
	public boolean canInput, canOutput;

	public IoDummyMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
		this(containerId, playerInv, extra.readBlockPos(), extra.readBoolean(), extra.readBoolean(), extra.readBoolean());
	}

	public IoDummyMenu(int containerId, Inventory playerInv, BlockPos pos, boolean item, boolean canInput, boolean canOutput) {
		this(containerId, playerInv, pos, item
				? new ItemStacksResourceHandler(16)
				: new FluidStacksResourceHandler(1, 16000), canInput, canOutput);
	}

	public IoDummyMenu(int containerId, Inventory playerInv, BlockPos pos, ResourceHandler<?> resourceHandler, boolean canInput, boolean canOutput) {
		super(ModMenuTypes.IO_DUMMY.get(), containerId, playerInv, InteractionHand.MAIN_HAND, m -> {
			IoDummyMenu menu = (IoDummyMenu) m;
			menu.pos = pos;
			menu.resourceHandler = resourceHandler;
			menu.canInput = canInput;
			menu.canOutput = canOutput;
			menu.count = 15;
			menu.init();
		});
		if (resourceHandler.getResource(0) instanceof ItemResource) {
			ItemStacksResourceHandler storage = (ItemStacksResourceHandler) resourceHandler;
			for (int i = 0; i < 16; i++)
				addSlot(new ResourceHandlerSlot(storage, storage::set, i, -99, -99));
		} else {
			addSlot(new Slot(fluidIoContainer, 0, -99, -99));
			addSlot(new Slot(fluidIoContainer, 1, -99, -99));
		}
	}

	@Override
	public void init() {
		initNetworkData();
		ioType = registerData("io_type", SyncMode.S2C, "");
		fluidId = registerData("fluid_id", SyncMode.S2C, "");
		fluidAmount = registerData("fluid_amount", SyncMode.S2C, 0);
		fluidCapacity = registerData("fluid_capacity", SyncMode.S2C, 0);
		fluidIoContainer = new SimpleContainer(2);
		if (resourceHandler.getResource(0) instanceof ItemResource) {
			ioType.set(canInput ? "item_input" : "item_output");
			return;
		}

		ioType.set(canInput ? "fluid_input" : "fluid_output");
		syncFluidData((ResourceHandler<FluidResource>) resourceHandler);
	}

	@Override
	public void tick() {
		if (playerInv.player.level().isClientSide()) return;
		if (count == 15) {
			count = 0;
			onNetworkSynced();
			if (resourceHandler.getResource(0) instanceof FluidResource)
				tryProcessFluidIo(fluidIoContainer, (ResourceHandler<FluidResource>) resourceHandler);
			playerInv.player.level().getBlockEntity(pos).setChanged();
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
	}

	@Override
	protected void onNetworkSynced() {
		if (resourceHandler.getResource(0) instanceof FluidResource)
			syncFluidData((ResourceHandler<FluidResource>) resourceHandler);
	}

	public void syncFluidData(ResourceHandler<FluidResource> handler) {
		FluidResource resource = handler.getResource(0);
		fluidId.set(resource.isEmpty() ? "" : BuiltInRegistries.FLUID.getKey(resource.getFluid()).toString());
		fluidAmount.set(handler.getAmountAsInt(0));
		fluidCapacity.set(handler.getCapacityAsInt(0, FluidResource.EMPTY));
	}
}
