package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemHandlerCompat
{
	public static ResourceHandler<ItemResource> asResourceHandler(IItemHandlerModifiable handler)
	{
		return new ItemHandlerResourceWrapper(handler);
	}

	public static Tag serializeNBT(Object handler, Provider provider)
	{
		if(handler instanceof SlotwiseItemHandler slotwise)
			return slotwise.serializeNBT(provider);
		if(handler instanceof ItemStackHandler itemStackHandler)
		{
			TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
			itemStackHandler.serialize(output);
			return output.buildResult();
		}
		return new CompoundTag();
	}

	public static void deserializeNBT(Object handler, Provider provider, CompoundTag nbt)
	{
		if(handler instanceof SlotwiseItemHandler slotwise)
			slotwise.deserializeNBT(provider, nbt);
		else if(handler instanceof ItemStackHandler itemStackHandler)
			itemStackHandler.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, provider, nbt));
	}

	private static class ItemHandlerResourceWrapper implements ResourceHandler<ItemResource>
	{
		private final IItemHandlerModifiable handler;
		private final List<SlotWrapper> slotWrappers = new ArrayList<>();

		private ItemHandlerResourceWrapper(IItemHandlerModifiable handler)
		{
			this.handler = handler;
			resize();
		}

		private void resize()
		{
			final int slotCount = handler.getSlots();
			while(slotWrappers.size() < slotCount)
				slotWrappers.add(new SlotWrapper(slotWrappers.size()));
			if(slotWrappers.size() > slotCount)
				slotWrappers.subList(slotCount, slotWrappers.size()).clear();
		}

		private SlotWrapper getSlotWrapper(int index)
		{
			resize();
			Objects.checkIndex(index, handler.getSlots());
			return slotWrappers.get(index);
		}

		@Override
		public int size()
		{
			return handler.getSlots();
		}

		@Override
		public ItemResource getResource(int index)
		{
			return getSlotWrapper(index).getResource(0);
		}

		@Override
		public long getAmountAsLong(int index)
		{
			return getSlotWrapper(index).getAmountAsLong(0);
		}

		@Override
		public long getCapacityAsLong(int index, ItemResource resource)
		{
			return getSlotWrapper(index).getCapacityAsLong(0, resource);
		}

		@Override
		public boolean isValid(int index, ItemResource resource)
		{
			return getSlotWrapper(index).isValid(0, resource);
		}

		@Override
		public int insert(int index, ItemResource resource, int amount, TransactionContext transaction)
		{
			return getSlotWrapper(index).insert(0, resource, amount, transaction);
		}

		@Override
		public int extract(int index, ItemResource resource, int amount, TransactionContext transaction)
		{
			return getSlotWrapper(index).extract(0, resource, amount, transaction);
		}

		private class SlotWrapper extends SnapshotJournal<ItemStack> implements ResourceHandler<ItemResource>
		{
			private final int outerSlot;

			private SlotWrapper(int outerSlot)
			{
				this.outerSlot = outerSlot;
			}

			@Override
			public int size()
			{
				return 1;
			}

			@Override
			public ItemResource getResource(int index)
			{
				Objects.checkIndex(index, size());
				return ItemResource.of(handler.getStackInSlot(outerSlot));
			}

			@Override
			public long getAmountAsLong(int index)
			{
				Objects.checkIndex(index, size());
				return handler.getStackInSlot(outerSlot).getCount();
			}

			@Override
			public long getCapacityAsLong(int index, ItemResource resource)
			{
				Objects.checkIndex(index, size());
				if(resource.isEmpty())
					return handler.getSlotLimit(outerSlot);
				if(!isValid(index, resource))
					return 0;
				return Math.min(handler.getSlotLimit(outerSlot), resource.getMaxStackSize());
			}

			@Override
			public boolean isValid(int index, ItemResource resource)
			{
				Objects.checkIndex(index, size());
				TransferPreconditions.checkNonEmpty(resource);
				return handler.isItemValid(outerSlot, resource.toStack());
			}

			@Override
			public int insert(int index, ItemResource resource, int amount, TransactionContext transaction)
			{
				Objects.checkIndex(index, size());
				TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
				if(amount==0)
					return 0;

				ItemStack toInsert = resource.toStack(amount);
				ItemStack simulatedRemainder = handler.insertItem(outerSlot, toInsert, true);
				int inserted = amount-simulatedRemainder.getCount();
				if(inserted <= 0)
					return 0;

				updateSnapshots(transaction);
				ItemStack actualRemainder = handler.insertItem(outerSlot, resource.toStack(inserted), false);
				return inserted-actualRemainder.getCount();
			}

			@Override
			public int extract(int index, ItemResource resource, int amount, TransactionContext transaction)
			{
				Objects.checkIndex(index, size());
				TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
				if(amount==0)
					return 0;

				ItemStack current = handler.getStackInSlot(outerSlot);
				if(current.isEmpty()||!resource.matches(current))
					return 0;

				ItemStack simulatedExtract = handler.extractItem(outerSlot, Math.min(amount, current.getCount()), true);
				if(simulatedExtract.isEmpty()||!resource.matches(simulatedExtract))
					return 0;

				updateSnapshots(transaction);
				ItemStack actualExtract = handler.extractItem(outerSlot, simulatedExtract.getCount(), false);
				return resource.matches(actualExtract)?actualExtract.getCount(): 0;
			}

			@Override
			protected ItemStack createSnapshot()
			{
				return handler.getStackInSlot(outerSlot).copy();
			}

			@Override
			protected void revertToSnapshot(ItemStack snapshot)
			{
				handler.setStackInSlot(outerSlot, snapshot);
			}
		}
	}
}
