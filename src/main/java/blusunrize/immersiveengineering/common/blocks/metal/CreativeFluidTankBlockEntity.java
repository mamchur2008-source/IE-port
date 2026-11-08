/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockEntityDrop;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import blusunrize.immersiveengineering.common.util.FluidStackCompat;
import blusunrize.immersiveengineering.common.util.ItemHandlerCompat;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class CreativeFluidTankBlockEntity extends IEBaseBlockEntity implements IBlockOverlayText,
		IPlayerInteraction, IBlockEntityDrop, IComparatorOverride, IInteractionObjectIE<CreativeFluidTankBlockEntity>
{
	public static final int SLOT_INPUT = 0;
	public static final int SLOT_OUTPUT = 1;
	public static final int NUM_SLOTS = 2;

	private final FluidTank tank = new FluidTank(FluidType.BUCKET_VOLUME);
	private final IFluidHandler pipeHandler = new CreativeTankFluidHandler(false);
	private final IFluidHandler bucketHandler = new CreativeTankFluidHandler(true);
	private boolean processingContainerSlots;
	private final ItemStackHandler inventory = new ItemStackHandler(NUM_SLOTS)
	{
		public int getSlotLimit(int slot)
		{
			return slot==SLOT_INPUT?64: 1;
		}

		protected void onContentsChanged(int slot)
		{
			if(!processingContainerSlots&&(slot==SLOT_INPUT||slot==SLOT_OUTPUT))
				processInputSlot();
			setChanged();
			if(level!=null&&!level.isClientSide())
				markContainingBlockForUpdate(null);
		}
	};

	public CreativeFluidTankBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.CREATIVE_FLUID_TANK.get(), pos, state);
	}

	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		setFluid(FluidStackCompat.parseOptional(provider, nbt.getCompoundOrEmpty("fluid")), false);
		if(!descPacket)
		{
			processingContainerSlots = true;
			ItemHandlerCompat.deserializeNBT(inventory, provider, nbt.getCompoundOrEmpty("inventory"));
			if(inventory.getSlots() < NUM_SLOTS)
			{
				ItemStack oldInput = inventory.getStackInSlot(0).copy();
				inventory.setSize(NUM_SLOTS);
				inventory.setStackInSlot(SLOT_INPUT, oldInput);
			}
			processingContainerSlots = false;
			processInputSlot();
		}
	}

	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		FluidStack stored = getStoredFluid();
		if(!stored.isEmpty())
			nbt.put("fluid", FluidStackCompat.saveOptional(stored, provider));
		if(!descPacket&&hasContainerItems())
		{
			Tag inventoryNBT = ItemHandlerCompat.serializeNBT(inventory, provider);
			nbt.put("inventory", inventoryNBT);
		}
	}

	public static void registerCapabilities(BECapabilityRegistrar<CreativeFluidTankBlockEntity> registrar)
	{
		registrar.register(Capabilities.Fluid.BLOCK, (be, side) -> be.pipeHandler);
	}

	public InteractionResult interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(player.isShiftKeyDown()&&FluidUtils.interactWithFluidHandler(player, hand, bucketHandler))
			return InteractionResult.SUCCESS;
		return InteractionResult.PASS;
	}

	@Nullable
	public Component[] getOverlayText(@Nullable BlockState blockState, Player player, HitResult rtr, boolean hammer)
	{
		if(rtr.getType()==Type.MISS)
			return null;
		if(Utils.isFluidRelatedItemStack(player.getItemInHand(InteractionHand.MAIN_HAND)))
		{
			FluidStack stored = getStoredFluid();
			if(stored.isEmpty())
				return new Component[]{TextUtils.formatFluidStack(FluidStack.EMPTY)};
			return new Component[]{Component.translatable(Lib.DESC_INFO+"creativeFluidTank", stored.getHoverName())};
		}
		return null;
	}

	public void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		FluidStack stored = getStoredFluid();
		if(!stored.isEmpty())
			stack.set(IEDataComponents.GENERIC_FLUID, SimpleFluidContent.copyOf(stored));
		drop.accept(stack);
		for(int slot = 0; slot < inventory.getSlots(); ++slot)
		{
			ItemStack slotStack = inventory.getStackInSlot(slot);
			if(!slotStack.isEmpty())
				drop.accept(slotStack.copy());
		}
	}

	public void onBEPlaced(BlockPlaceContext ctx)
	{
		setFluid(ctx.getItemInHand().getOrDefault(IEDataComponents.GENERIC_FLUID, SimpleFluidContent.EMPTY).copy(), false);
	}

	public int getComparatorInputOverride()
	{
		return getStoredFluid().isEmpty()?0: 15;
	}

	public FluidTank getTank()
	{
		return tank;
	}

	public ItemStackHandler getInventory()
	{
		return inventory;
	}

	public FluidStack getStoredFluid()
	{
		return tank.getFluid();
	}

	public void clearFluid()
	{
		setFluid(FluidStack.EMPTY, true);
	}

	public void setFluid(FluidStack newFluid, boolean notify)
	{
		newFluid = normalizeFluid(newFluid);
		if(FluidStack.isSameFluidSameComponents(tank.getFluid(), newFluid))
			return;
		tank.setFluid(newFluid);
		setChanged();
		if(notify)
			markContainingBlockForUpdate(null);
	}

	private boolean hasContainerItems()
	{
		for(int slot = 0; slot < inventory.getSlots(); ++slot)
			if(!inventory.getStackInSlot(slot).isEmpty())
				return true;
		return false;
	}

	private void processInputSlot()
	{
		if(level==null||level.isClientSide())
			return;
		ItemStack stack = inventory.getStackInSlot(SLOT_INPUT);
		if(stack.isEmpty())
			return;

		boolean filledContainer = FluidUtil.getFluidContained(stack).filter(fluid -> !fluid.isEmpty()).isPresent();
		FluidActionResult simulated = FluidActionResult.FAILURE;
		if(filledContainer)
			simulated = FluidUtil.tryEmptyContainer(stack, bucketHandler, Integer.MAX_VALUE, null, false);
		else if(!getStoredFluid().isEmpty())
			simulated = FluidUtil.tryFillContainer(stack, pipeHandler, Integer.MAX_VALUE, null, false);
		if(!simulated.isSuccess()||!canMoveToOutput(simulated.getResult()))
			return;

		FluidActionResult result = filledContainer?
				FluidUtil.tryEmptyContainer(stack, bucketHandler, Integer.MAX_VALUE, null, true):
				FluidUtil.tryFillContainer(stack, pipeHandler, Integer.MAX_VALUE, null, true);
		if(!result.isSuccess())
			return;

		processingContainerSlots = true;
		try
		{
			ItemStack remainingInput = stack.copy();
			remainingInput.shrink(1);
			inventory.setStackInSlot(SLOT_INPUT, remainingInput);
			moveToOutput(result.getResult());
		} finally
		{
			processingContainerSlots = false;
		}
		setChanged();
		markContainingBlockForUpdate(null);
	}

	private boolean canMoveToOutput(ItemStack result)
	{
		if(result.isEmpty())
			return true;
		ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
		if(output.isEmpty())
			return true;
		if(!ItemStack.isSameItemSameComponents(output, result))
			return false;
		return output.getCount()+result.getCount() <= Math.min(output.getMaxStackSize(), inventory.getSlotLimit(SLOT_OUTPUT));
	}

	private void moveToOutput(ItemStack result)
	{
		if(result.isEmpty())
			return;
		ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
		if(output.isEmpty())
			inventory.setStackInSlot(SLOT_OUTPUT, result.copy());
		else
		{
			output.grow(result.getCount());
			inventory.setStackInSlot(SLOT_OUTPUT, output);
		}
	}

	@Nonnull
	public Component getDisplayName()
	{
		return Component.translatable("block."+Lib.MODID+".creative_fluid_tank");
	}

	public boolean canUseGui(Player player)
	{
		return true;
	}

	public CreativeFluidTankBlockEntity getGuiMaster()
	{
		return this;
	}

	public ArgContainer<CreativeFluidTankBlockEntity, ?> getContainerType()
	{
		return IEMenuTypes.CREATIVE_FLUID_TANK;
	}

	private static FluidStack normalizeFluid(FluidStack stack)
	{
		if(stack.isEmpty())
			return FluidStack.EMPTY;
		return stack.copyWithAmount(FluidType.BUCKET_VOLUME);
	}

	private class CreativeTankFluidHandler implements IFluidHandler
	{
		private final boolean allowSettingFluid;

		private CreativeTankFluidHandler(boolean allowSettingFluid)
		{
			this.allowSettingFluid = allowSettingFluid;
		}

		public int getTanks()
		{
			return 1;
		}

		@Nonnull
		public FluidStack getFluidInTank(int tank)
		{
			FluidStack stored = getStoredFluid();
			if(tank!=0||stored.isEmpty())
				return FluidStack.EMPTY;
			return stored.copyWithAmount(Integer.MAX_VALUE);
		}

		public int getTankCapacity(int tank)
		{
			return tank==0?Integer.MAX_VALUE: 0;
		}

		public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
		{
			return tank==0&&!stack.isEmpty();
		}

		public int fill(FluidStack resource, FluidAction action)
		{
			if(!allowSettingFluid||resource.isEmpty())
				return 0;
			if(action.execute())
				setFluid(resource, true);
			return resource.getAmount();
		}

		@Nonnull
		public FluidStack drain(FluidStack resource, FluidAction action)
		{
			FluidStack stored = getStoredFluid();
			if(resource.isEmpty()||stored.isEmpty()||!FluidStack.isSameFluidSameComponents(resource, stored))
				return FluidStack.EMPTY;
			return stored.copyWithAmount(resource.getAmount());
		}

		@Nonnull
		public FluidStack drain(int maxDrain, FluidAction action)
		{
			FluidStack stored = getStoredFluid();
			if(maxDrain <= 0||stored.isEmpty())
				return FluidStack.EMPTY;
			return stored.copyWithAmount(maxDrain);
		}
	}
}
