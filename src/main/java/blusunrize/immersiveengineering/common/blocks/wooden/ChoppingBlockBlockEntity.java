/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.util.ContainerHelperCompat;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

public class ChoppingBlockBlockEntity extends IEBaseBlockEntity implements IIEInventory, IPlayerInteraction, IEClientTickableBE
{
	private static final int KINDLING_PER_LOG = 4;
	private static final int CHOP_ANIMATION_EVENT = 1;
	private static final int CHOP_ANIMATION_TICKS = 8;
	private final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
	private ItemStack animationLog = ItemStack.EMPTY;
	private int prevChopAnimationTicks = -1;
	private int chopAnimationTicks = -1;

	public ChoppingBlockBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.CHOPPING_BLOCK.get(), pos, state);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		ContainerHelperCompat.loadAllItems(nbt, inventory, provider);
		clampStoredLog();
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		ContainerHelperCompat.saveAllItems(nbt, inventory, provider);
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return stack.is(ItemTags.LOGS_THAT_BURN);
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 1;
	}

	@Override
	public boolean canTickAny()
	{
		return chopAnimationTicks >= 0;
	}

	@Override
	public void tickClient()
	{
		prevChopAnimationTicks = chopAnimationTicks;
		if(chopAnimationTicks >= 0)
		{
			chopAnimationTicks--;
			if(chopAnimationTicks < 0)
				animationLog = ItemStack.EMPTY;
		}
	}

	@Override
	public void doGraphicalUpdates()
	{
		setChanged();
		markContainingBlockForUpdate(null);
	}

	@Override
	public InteractionResult interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		clampStoredLog();
		ItemStack stored = inventory.get(0);
		InteractionHand otherHand = hand==InteractionHand.MAIN_HAND?InteractionHand.OFF_HAND: InteractionHand.MAIN_HAND;
		ItemStack otherHeld = player.getItemInHand(otherHand);
		if(stored.isEmpty())
		{
			if(!isStackValid(0, heldItem))
				return InteractionResult.PASS;
			if(!getLevelNonnull().isClientSide())
				setStoredLog(player, hand, heldItem);
			return InteractionResult.SUCCESS;
		}

		if(heldItem.is(ItemTags.AXES))
			return InteractionResult.PASS;
		else if(otherHeld.is(ItemTags.AXES))
			return InteractionResult.PASS;
		else if(heldItem.isEmpty())
		{
			if(!getLevelNonnull().isClientSide())
				removeStoredLog(player, hand, stored);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.FAIL;
	}

	public boolean chopWithAxe(Player player, InteractionHand hand, ItemStack tool)
	{
		if(!tool.is(ItemTags.AXES))
			return false;
		clampStoredLog();
		if(inventory.get(0).isEmpty())
			return false;
		player.swing(hand);
		if(getLevelNonnull().isClientSide())
			startChopAnimation(inventory.get(0));
		else
			chopStoredLog(player, hand, tool);
		return true;
	}

	private void setStoredLog(Player player, InteractionHand hand, ItemStack heldItem)
	{
		inventory.set(0, heldItem.copyWithCount(1));
		if(!player.getAbilities().instabuild)
		{
			heldItem.shrink(1);
			if(heldItem.isEmpty())
				player.setItemInHand(hand, ItemStack.EMPTY);
		}
		updateLoadedState(true);
	}

	private void clampStoredLog()
	{
		ItemStack stored = inventory.get(0);
		if(!stored.isEmpty()&&stored.getCount()!=1)
			inventory.set(0, stored.copyWithCount(1));
	}

	private void chopStoredLog(Player player, InteractionHand hand, ItemStack tool)
	{
		broadcastChopAnimation(getChopSide(player));
		inventory.set(0, ItemStack.EMPTY);
		giveKindling(player);
		damageAxe(player, hand, tool);
		playChopSound();
		updateLoadedState(false);
	}

	private void giveKindling(Player player)
	{
		ItemStack kindling = new ItemStack(Ingredients.KINDLING, KINDLING_PER_LOG);
		if(!player.getInventory().add(kindling))
			player.drop(kindling, false, true);
	}

	private void damageAxe(Player player, InteractionHand hand, ItemStack tool)
	{
		if(!player.getAbilities().instabuild)
			tool.hurtAndBreak(1, player, hand==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND: EquipmentSlot.OFFHAND);
	}

	private void playChopSound()
	{
		Level level = getLevelNonnull();
		level.playSound(null, getBlockPos(), SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1, 0.85F+level.getRandom().nextFloat()*0.25F);
	}

	private void broadcastChopAnimation(Direction chopSide)
	{
		Level level = getLevelNonnull();
		level.blockEvent(getBlockPos(), getBlockState().getBlock(), CHOP_ANIMATION_EVENT, chopSide.get3DDataValue());
	}

	private void removeStoredLog(Player player, InteractionHand hand, ItemStack stored)
	{
		player.setItemInHand(hand, stored.copy());
		inventory.set(0, ItemStack.EMPTY);
		updateLoadedState(false);
	}

	private void updateLoadedState(boolean hasLog)
	{
		BlockState state = getLevelNonnull().getBlockState(getBlockPos());
		if(state.hasProperty(ChoppingBlockBlock.HAS_LOG)&&state.getValue(ChoppingBlockBlock.HAS_LOG)!=hasLog)
			getLevelNonnull().setBlockAndUpdate(getBlockPos(), state.setValue(ChoppingBlockBlock.HAS_LOG, hasLog));
		doGraphicalUpdates();
	}

	@Override
	public boolean triggerEvent(int id, int type)
	{
		if(id==CHOP_ANIMATION_EVENT)
		{
			if(getLevel()!=null&&getLevel().isClientSide()&&chopAnimationTicks < 0)
			{
				ItemStack renderedLog = getLogStackForRendering();
				if(renderedLog.isEmpty())
					renderedLog = new ItemStack(Items.OAK_LOG);
				startChopAnimation(renderedLog);
			}
			return true;
		}
		return super.triggerEvent(id, type);
	}

	public ItemStack getLogStackForRendering()
	{
		ItemStack stored = inventory.get(0);
		if(!stored.isEmpty())
			return stored;
		if(isChopAnimationActive()&&!animationLog.isEmpty())
			return animationLog;
		return ItemStack.EMPTY;
	}

	public boolean isChopAnimationActive()
	{
		return chopAnimationTicks >= 0&&!animationLog.isEmpty();
	}

	public float getChopAnimation(float partialTicks)
	{
		if(!isChopAnimationActive())
			return 0;
		float ticksRemaining = Mth.lerp(partialTicks, prevChopAnimationTicks, chopAnimationTicks);
		return 1-Mth.clamp(ticksRemaining/CHOP_ANIMATION_TICKS, 0, 1);
	}

	private void startChopAnimation(ItemStack log)
	{
		if(log.isEmpty())
			return;
		animationLog = log.copyWithCount(1);
		prevChopAnimationTicks = CHOP_ANIMATION_TICKS;
		chopAnimationTicks = CHOP_ANIMATION_TICKS;
	}

	private Direction getChopSide(Player player)
	{
		double dx = player.getX()-(getBlockPos().getX()+.5);
		double dz = player.getZ()-(getBlockPos().getZ()+.5);
		if(Math.abs(dx) > Math.abs(dz))
			return dx < 0?Direction.WEST: Direction.EAST;
		return dz < 0?Direction.NORTH: Direction.SOUTH;
	}

	@EventBusSubscriber(modid = MODID)
	public static class ChoppingBlockInteractionHandler
	{
		@SubscribeEvent(priority = EventPriority.HIGHEST)
		public static void onLeftClickBlock(LeftClickBlock event)
		{
			BlockState state = event.getLevel().getBlockState(event.getPos());
			if(!(state.getBlock() instanceof ChoppingBlockBlock)||event.getEntity().isShiftKeyDown())
				return;

			ItemStack tool = event.getEntity().getMainHandItem();
			if(!tool.is(ItemTags.AXES))
				return;

			event.setCanceled(true);
			if(event.getAction()!=LeftClickBlock.Action.START
					||!state.hasProperty(ChoppingBlockBlock.HAS_LOG)||!state.getValue(ChoppingBlockBlock.HAS_LOG))
				return;

			if(event.getLevel().getBlockEntity(event.getPos()) instanceof ChoppingBlockBlockEntity choppingBlock)
				choppingBlock.chopWithAxe(event.getEntity(), InteractionHand.MAIN_HAND, tool);
		}
	}
}
