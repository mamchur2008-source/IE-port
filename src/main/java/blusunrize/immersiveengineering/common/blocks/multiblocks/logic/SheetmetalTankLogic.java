/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MBInventoryUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.SheetmetalTankLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.interfaces.MBMemorizeStructure;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.interfaces.MBOverlayText;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.SiloTankShapes;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.util.ItemHandlerCompat;
import blusunrize.immersiveengineering.common.util.LayeredComparatorOutput;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SheetmetalTankLogic implements IServerTickableComponent<State>, MBOverlayText<State>, MBMemorizeStructure<State>
{
	private static final SiloTankShapes SHAPE_GETTER = new SiloTankShapes(4, false);
	public static final BlockPos IO_POS = new BlockPos(1, 0, 1);
	private static final BlockPos INPUT_POS = new BlockPos(1, 4, 1);

	public void tickServer(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		state.processInputSlot();
		state.comparatorHelper.update(context, state.tank.getFluidAmount());
		if(!state.rsState.isEnabled(context)||state.tank.isEmpty())
			return;
		for(Supplier<@Nullable IFluidHandler> outputRef : state.outputs)
		{
			int outSize = Math.min(FluidType.BUCKET_VOLUME, state.tank.getFluidAmount());
			FluidStack out = state.tank.getFluid().copyWithAmount(outSize);
			IFluidHandler output = outputRef.get();
			if(output==null)
				continue;
			int accepted = output.fill(out, FluidAction.SIMULATE);
			if(accepted > 0)
			{
				int drained = output.fill(out.copyWithAmount(Math.min(out.getAmount(), accepted)), FluidAction.EXECUTE);
				state.tank.drain(drained, FluidAction.EXECUTE);
				context.markMasterDirty();
				context.requestMasterBESync();
				if(state.tank.isEmpty())
					break;
			}
		}
	}

	public State createInitialState(IInitialMultiblockContext<State> capabilitySource)
	{
		return new State(capabilitySource);
	}

	public void registerCapabilities(CapabilityRegistrar<State> register)
	{
		register.register(Capabilities.Fluid.BLOCK, (state, position) -> {
			if(IO_POS.equals(position.posInMultiblock()))
				return state.ioHandler;
			else if(INPUT_POS.equals(position.posInMultiblock()))
				return state.inputHandler;
			else
				return null;
		});
	}

	public void dropExtraItems(State state, Consumer<ItemStack> drop)
	{
		MBInventoryUtils.dropItems(state.inventory, drop);
	}

	@Nullable
	public List<Component> getOverlayText(State state, BlockPos posInMultiblock, BlockHitResult absoluteHit, Player player, boolean hammer)
	{
		if(Utils.isFluidRelatedItemStack(player.getItemInHand(InteractionHand.MAIN_HAND)))
			return List.of(TextUtils.formatFluidStack(state.tank.getFluid()));
		return null;
	}

	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType)
	{
		return SHAPE_GETTER;
	}

	public InteractionResult click(
			IMultiblockContext<State> ctx, BlockPos posInMultiblock,
			Player player, InteractionHand hand, BlockHitResult absoluteHit,
			boolean isClient
	)
	{
		if(FluidUtils.interactWithFluidHandler(player, hand, ctx.getState().tank))
		{
			ctx.markDirtyAndSync();
			return InteractionResult.SUCCESS;
		}
		else if(hand==InteractionHand.MAIN_HAND&&!player.isShiftKeyDown())
		{
			if(!isClient)
				player.openMenu(IEMenuTypes.TANK.provide(ctx, posInMultiblock));
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	public void setMemorizedBlockState(State state, BlockPos pos, BlockState blockState)
	{
		state.structureMemo.put(pos, blockState);
	}

	public BlockState getMemorizedBlockState(State state, BlockPos pos)
	{
		return state.structureMemo.get(pos);
	}

	public static class State implements IMultiblockState
	{
		public static final int SLOT_INPUT = 0;
		public static final int SLOT_OUTPUT = 1;
		public static final int NUM_SLOTS = 2;

		public final FluidTank tank = new FluidTank(512*FluidType.BUCKET_VOLUME);
		public final ItemStackHandler inventory;
		private final LayeredComparatorOutput<IMultiblockContext<?>> comparatorHelper;
		private final List<Supplier<@Nullable IFluidHandler>> outputs;
		private final IFluidHandler inputHandler;
		private final IFluidHandler ioHandler;
		public final RSState rsState = RSState.disabledByDefault();
		private final StructureMemo structureMemo = new StructureMemo();
		private final Runnable changedAndSync;
		private boolean processingContainerSlots;

		public State(IInitialMultiblockContext<State> capabilitySource)
		{
			this.comparatorHelper = LayeredComparatorOutput.makeForSiloLike(tank.getCapacity(), 4);
			ImmutableList.Builder<Supplier<@Nullable IFluidHandler>> outputBuilder = ImmutableList.builder();
			for(RelativeBlockFace face : RelativeBlockFace.values())
				if(face!=RelativeBlockFace.DOWN)
				{
					final BlockPos neighbor = face.offsetRelative(IO_POS, -1);
					outputBuilder.add(capabilitySource.getCapabilityAt(Capabilities.Fluid.BLOCK, neighbor, face));
				}
			this.outputs = outputBuilder.build();
			this.changedAndSync = () -> {
				capabilitySource.getSyncRunnable().run();
				capabilitySource.getMarkDirtyRunnable().run();
			};
			this.inputHandler = new ArrayFluidHandler(tank, false, true, changedAndSync);
			this.ioHandler = new ArrayFluidHandler(tank, true, true, changedAndSync);
			this.inventory = new ItemStackHandler(NUM_SLOTS)
			{
				public int getSlotLimit(int slot)
				{
					return slot==SLOT_INPUT?64: 64;
				}

				protected void onContentsChanged(int slot)
				{
					if(processingContainerSlots)
						return;
					if(slot==SLOT_INPUT||slot==SLOT_OUTPUT)
						processInputSlot();
					changedAndSync.run();
				}
			};
		}

		public void writeSaveNBT(CompoundTag nbt, Provider provider)
		{
			nbt.put("tank", blusunrize.immersiveengineering.common.util.FluidTankCompat.writeToNBT(tank, provider));
			if(hasContainerItems())
				nbt.put("inventory", ItemHandlerCompat.serializeNBT(inventory, provider));
			structureMemo.writeSaveNBT(nbt, provider);
		}

		public void readSaveNBT(CompoundTag nbt, Provider provider)
		{
			readNBT(nbt, provider, true);
		}

		public void writeSyncNBT(CompoundTag nbt, Provider provider)
		{
			writeSaveNBT(nbt, provider);
		}

		public void readSyncNBT(CompoundTag nbt, Provider provider)
		{
			readNBT(nbt, provider, false);
		}

		private void readNBT(CompoundTag nbt, Provider provider, boolean processContainers)
		{
			blusunrize.immersiveengineering.common.util.FluidTankCompat.readFromNBT(tank, provider, nbt.getCompoundOrEmpty("tank"));
			processingContainerSlots = true;
			Tag inventoryNBT = nbt.get("inventory");
			if(inventoryNBT instanceof CompoundTag inventoryTag)
				ItemHandlerCompat.deserializeNBT(inventory, provider, inventoryTag);
			else
				ItemHandlerCompat.deserializeNBT(inventory, provider, new CompoundTag());
			processingContainerSlots = false;
			if(processContainers)
				processInputSlot();
			structureMemo.readSaveNBT(nbt, provider);
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
			ItemStack stack = inventory.getStackInSlot(SLOT_INPUT);
			if(stack.isEmpty())
				return;

			boolean filledContainer = FluidUtil.getFluidContained(stack).filter(fluid -> !fluid.isEmpty()).isPresent();
			FluidActionResult simulated = FluidActionResult.FAILURE;
			if(filledContainer)
				simulated = FluidUtil.tryEmptyContainer(stack, inputHandler, Integer.MAX_VALUE, null, false);
			else if(!tank.isEmpty())
				simulated = FluidUtil.tryFillContainer(stack, ioHandler, Integer.MAX_VALUE, null, false);
			if(!simulated.isSuccess()||!canMoveToOutput(simulated.getResult()))
				return;

			FluidActionResult result = filledContainer?
					FluidUtil.tryEmptyContainer(stack, inputHandler, Integer.MAX_VALUE, null, true):
					FluidUtil.tryFillContainer(stack, ioHandler, Integer.MAX_VALUE, null, true);
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
			changedAndSync.run();
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
	}
}
