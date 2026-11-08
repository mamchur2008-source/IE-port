/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class ModWorkbenchBlockEntity extends IEBaseBlockEntity implements IIEInventory, IStateBasedDirectional,
		IHasDummyBlocks, IInteractionObjectIE<ModWorkbenchBlockEntity>, IModelOffsetProvider
{
	public static final BlockPos MASTER_POS = BlockPos.ZERO;
	public static final BlockPos DUMMY_POS = new BlockPos(1, 0, 0);
	private final NonNullList<ItemStack> inventory = NonNullList.withSize(7, ItemStack.EMPTY);

	public ModWorkbenchBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.MOD_WORKBENCH.get(), pos, state);
	}

	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		blusunrize.immersiveengineering.common.util.ContainerHelperCompat.loadAllItems(nbt, inventory, provider);
	}

	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		blusunrize.immersiveengineering.common.util.ContainerHelperCompat.saveAllItems(nbt, inventory, provider);
	}

	public AABB renderAABB;

	public NonNullList<ItemStack> getInventory()
	{
		return this.inventory;
	}

	public boolean isStackValid(int slot, ItemStack stack)
	{
		return true;
	}

	public int getSlotLimit(int slot)
	{
		return slot==0?1: 64;
	}

	public void doGraphicalUpdates()
	{
	}

	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return false;
	}

	public void receiveMessageFromClient(CompoundTag message)
	{
		applyConfigTo(inventory.get(0), message);
	}

	public static void applyConfigTo(ItemStack stack, CompoundTag message)
	{
		if(!(stack.getItem() instanceof IConfigurableTool))
			return;
		for(String key : message.keySet())
		{
			Tag tag = message.get(key);
			if(tag instanceof ByteTag)
				((IConfigurableTool)stack.getItem()).applyConfigOption(stack, key, ((ByteTag)tag).byteValue()!=0);
			else if(tag instanceof FloatTag)
				((IConfigurableTool)stack.getItem()).applyConfigOption(stack, key, ((FloatTag)tag).floatValue());
		}
	}

	public boolean isDummy()
	{
		return getState().getValue(IEProperties.MULTIBLOCKSLAVE);
	}

	@Nullable
	public ModWorkbenchBlockEntity master()
	{
		if(!isDummy())
			return this;
		// Used to provide tile-dependant drops after breaking
		if(tempMasterBE!=null)
			return (ModWorkbenchBlockEntity)tempMasterBE;
		Direction dummyDir = isDummy()?getFacing().getCounterClockWise(): getFacing().getClockWise();
		BlockPos masterPos = getBlockPos().relative(dummyDir);
		BlockEntity te = Utils.getExistingTileEntity(level, masterPos);
		return (te instanceof ModWorkbenchBlockEntity)?(ModWorkbenchBlockEntity)te: null;
	}

	public void placeDummies(BlockPlaceContext ctx, BlockState state)
	{
		DeskBlock.placeDummies(getBlockState(), level, worldPosition, ctx);
	}

	public void breakDummies(BlockPos pos, BlockState state)
	{
		tempMasterBE = master();
		Direction dummyDir = isDummy()?getFacing().getCounterClockWise(): getFacing().getClockWise();
		level.removeBlock(pos.relative(dummyDir), false);
	}

	public boolean canUseGui(Player player)
	{
		return true;
	}

	public ModWorkbenchBlockEntity getGuiMaster()
	{
		if(!isDummy())
			return this;
		Direction dummyDir = getFacing().getCounterClockWise();
		BlockEntity tileEntityModWorkbench = level.getBlockEntity(worldPosition.relative(dummyDir));
		if(tileEntityModWorkbench instanceof ModWorkbenchBlockEntity)
			return (ModWorkbenchBlockEntity)tileEntityModWorkbench;
		return null;
	}

	public ArgContainer<ModWorkbenchBlockEntity, ?> getContainerType()
	{
		return IEMenuTypes.MOD_WORKBENCH;
	}

	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	public BlockPos getModelOffset(BlockState state, @Nullable Vec3i size)
	{
		if(isDummy())
			return DUMMY_POS;
		else
			return MASTER_POS;
	}
}
