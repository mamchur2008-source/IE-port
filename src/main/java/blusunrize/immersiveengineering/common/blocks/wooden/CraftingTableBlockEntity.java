/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import blusunrize.immersiveengineering.common.util.inventory.IDropInventory;
import com.google.common.collect.Streams;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

public class CraftingTableBlockEntity extends IEBaseBlockEntity
		implements IStateBasedDirectional, IInteractionObjectIE<CraftingTableBlockEntity>, IDropInventory
{
	public static final int GRID_SIZE = 3;
	public static final int STORAGE_SIZE = 18;

	private final NonNullList<ItemStack> inventory = NonNullList.withSize(STORAGE_SIZE, ItemStack.EMPTY);
	private final NonNullList<ItemStack> craftingInv = NonNullList.withSize(GRID_SIZE*GRID_SIZE, ItemStack.EMPTY);

	public CraftingTableBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.CRAFTING_TABLE.get(), pos, state);
	}

	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		if(!descPacket)
		{
			NonNullList<ItemStack> totalInv = NonNullList.withSize(inventory.size()+craftingInv.size(), ItemStack.EMPTY);
			blusunrize.immersiveengineering.common.util.ContainerHelperCompat.loadAllItems(nbt, totalInv, provider);
			for(int i = 0; i < inventory.size(); ++i)
				inventory.set(i, totalInv.get(i));
			for(int i = 0; i < craftingInv.size(); ++i)
				craftingInv.set(i, totalInv.get(inventory.size()+i));
		}
	}

	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		if(!descPacket)
		{
			NonNullList<ItemStack> totalInv = NonNullList.withSize(inventory.size()+craftingInv.size(), ItemStack.EMPTY);
			for(int i = 0; i < inventory.size(); ++i)
				totalInv.set(i, inventory.get(i));
			for(int i = 0; i < craftingInv.size(); ++i)
				totalInv.set(inventory.size()+i, craftingInv.get(i));
			blusunrize.immersiveengineering.common.util.ContainerHelperCompat.saveAllItems(nbt, totalInv, provider);
		}
	}

	@Nonnull
	public Component getDisplayName()
	{
		return Component.translatable("block.immersiveengineering.craftingtable");
	}

	public boolean canUseGui(Player player)
	{
		return true;
	}

	public CraftingTableBlockEntity getGuiMaster()
	{
		return this;
	}

	public ArgContainer<CraftingTableBlockEntity, ?> getContainerType()
	{
		return IEMenuTypes.CRAFTING_TABLE;
	}

	private final IItemHandler inventoryCap = new ItemStackHandler(inventory)
	{
		protected void onContentsChanged(int slot)
		{
			super.onContentsChanged(slot);
			CraftingTableBlockEntity.this.setChanged();
		}
	};

	public static void registerCapabilities(BECapabilityRegistrar<CraftingTableBlockEntity> registrar)
	{
		registrar.registerAllContexts(Capabilities.Item.BLOCK, be -> be.inventoryCap);
	}

	public IItemHandler getInventoryCap()
	{
		return inventoryCap;
	}

	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	public NonNullList<ItemStack> getCraftingInventory()
	{
		return craftingInv;
	}

	public Stream<ItemStack> getDroppedItems()
	{
		return Streams.concat(craftingInv.stream(), inventory.stream());
	}
}
