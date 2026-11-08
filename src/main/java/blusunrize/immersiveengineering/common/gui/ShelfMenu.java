/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.ShelfLogic;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateBlockEntity;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerCopySlot;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class ShelfMenu extends IEContainerMenu
{
	public static final int MAX_SLOTS = 8*WoodenCrateBlockEntity.CONTAINER_SIZE;
	public static final int COLUMN_WIDTH = 176;
	public static final int CRATE_SEGMENT = 69;
	public static final int INV_SEGMENT = 94;

	public final GetterAndSetter<List<ItemStack>> cratesFront;
	public final GetterAndSetter<List<ItemStack>> cratesBack;
	public final GetterAndSetter<Boolean> backside;

	public static ShelfMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<ShelfLogic.State> ctx
	)
	{
		final ShelfLogic.State state = ctx.mbContext().getState();
		BlockPos pos = ctx.mbContext().getLevel().toRelative(ctx.clickedPos());
		return new ShelfMenu(
				multiblockCtx(type, id, ctx),
				state.getMenuItemHandler(pos),
				invPlayer,
				GetterAndSetter.getterOnly(() -> state.getCratesForMenu(pos, false)),
				GetterAndSetter.getterOnly(() -> state.getCratesForMenu(pos, true)),
				GetterAndSetter.standalone(false)
		);
	}

	public static ShelfMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new ShelfMenu(
				clientCtx(type, id),
				new ItemStackHandler(MAX_SLOTS),
				invPlayer,
				GetterAndSetter.standalone(List.of()),
				GetterAndSetter.standalone(List.of()),
				GetterAndSetter.standalone(false)
		);
	}

	public ShelfMenu(
			MenuContext ctx, IItemHandlerModifiable inventory, Inventory inventoryPlayer,
			GetterAndSetter<List<ItemStack>> cratesFront, GetterAndSetter<List<ItemStack>> cratesBack,
			GetterAndSetter<Boolean> backside
	)
	{
		super(ctx);
		this.cratesFront = cratesFront;
		this.cratesBack = cratesBack;
		this.backside = backside;
		addGenericData(new GenericContainerData<>(GenericDataSerializers.ITEM_STACKS, cratesFront));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.ITEM_STACKS, cratesBack));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.BOOLEAN, backside));

		// Bind all slots for crates, even those not present. They are toggled on/off dynamically.
		for(int iCrate = 0; iCrate < 4; iCrate++)
		{
			int x = iCrate%2*COLUMN_WIDTH;
			int y = iCrate/2*CRATE_SEGMENT;

			final int crateIdx = iCrate;
			for(int iSlot = 0; iSlot < WoodenCrateBlockEntity.CONTAINER_SIZE; iSlot++)
			{
				int frontIndex = iCrate*WoodenCrateBlockEntity.CONTAINER_SIZE+iSlot;
				this.addSlot(new ShelfSlot(inventory, frontIndex, 8+x+(iSlot%9)*18, 13+y+(iSlot/9)*18, crateIdx, () -> !backside.get(), cratesFront));
				int backIndex = (4+iCrate)*WoodenCrateBlockEntity.CONTAINER_SIZE+iSlot;
				this.addSlot(new ShelfSlot(inventory, backIndex, 8+x+(iSlot%9)*18, 13+y+(iSlot/9)*18, crateIdx, backside, cratesBack));
			}
		}
		ownSlotCount = MAX_SLOTS;

		// Bind player inventory
		int playerInvX = COLUMN_WIDTH/2;
		int playerInvY = 2*CRATE_SEGMENT;
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+playerInvX+j*18, 13+playerInvY+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+playerInvX+i*18, 71+playerInvY));
	}

	public void receiveMessageFromScreen(CompoundTag nbt)
	{
		super.receiveMessageFromScreen(nbt);
		if(nbt.contains("backside"))
			backside.set(nbt.getBooleanOr("backside", false));
	}

	@Nonnull
	public ItemStack quickMoveStack(Player player, int slot)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slotObject = this.slots.get(slot);
		if(slotObject.hasItem())
		{
			ItemStack itemstack1 = slotObject.getItem();
			itemstack = itemstack1.copy();
			if(slot < ownSlotCount)
			{
				if(!this.moveItemStackTo(itemstack1, ownSlotCount, this.slots.size(), true))
					return ItemStack.EMPTY;
			}
			else if(!this.moveToMatchingSlotOrAdjacent(itemstack1, 0, ownSlotCount))
				return ItemStack.EMPTY;

			if(itemstack1.isEmpty())
				slotObject.set(ItemStack.EMPTY);
			else
				slotObject.setChanged();
		}
		return itemstack;
	}

	private static final class ShelfSlot extends ItemHandlerCopySlot
	{
		private final int crateIndex;
		private final Supplier<Boolean> isActive;
		private final Supplier<List<ItemStack>> getCrates;

		public ShelfSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, int crateIndex, Supplier<Boolean> isActive, Supplier<List<ItemStack>> getCrates)
		{
			super(itemHandler, index, xPosition, yPosition);
			this.crateIndex = crateIndex;
			this.isActive = isActive;
			this.getCrates = getCrates;
		}

		public boolean isActive()
		{
			if(!isActive.get())
				return false;
			List<ItemStack> crates = getCrates.get();
			return crateIndex < crates.size()&&!crates.get(crateIndex).isEmpty();
		}

		public boolean mayPlace(ItemStack stack)
		{
			return super.mayPlace(stack)&&isActive();
		}
	}
}
