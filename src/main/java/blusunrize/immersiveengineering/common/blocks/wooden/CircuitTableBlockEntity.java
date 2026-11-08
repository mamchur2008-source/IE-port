/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitInstruction;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities.Energy;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class CircuitTableBlockEntity extends IEBaseBlockEntity implements IIEInventory, IStateBasedDirectional,
		IHasDummyBlocks, IInteractionObjectIE<CircuitTableBlockEntity>, IModelOffsetProvider
{
	public static final BlockPos MASTER_POS = BlockPos.ZERO;
	public static final BlockPos DUMMY_POS = new BlockPos(1, 0, 0);
	public static final String[] SLOT_TYPES = new String[]{"backplane", "logic", "solder"};

	public static final int ASSEMBLY_ENERGY = 5000;
	public static final int ENERGY_CAPACITY = 32000;
	public static final int NUM_SLOTS = SLOT_TYPES.length+1;

	public final MutableEnergyStorage energyStorage = new MutableEnergyStorage(ENERGY_CAPACITY);
	private final NonNullList<ItemStack> inventory = NonNullList.withSize(NUM_SLOTS, ItemStack.EMPTY);

	public CircuitTableBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.CIRCUIT_TABLE.get(), pos, state);
	}

	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		EnergyHelper.deserializeFrom(energyStorage, nbt, provider);
		if(!descPacket)
			blusunrize.immersiveengineering.common.util.ContainerHelperCompat.loadAllItems(nbt, inventory, provider);
	}

	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		EnergyHelper.serializeTo(energyStorage, nbt, provider);
		if(!descPacket)
			blusunrize.immersiveengineering.common.util.ContainerHelperCompat.saveAllItems(nbt, inventory, provider);
	}

	public static int getEditSlot()
	{
		return SLOT_TYPES.length;
	}

	public static int getIngredientAmount(LogicCircuitInstruction instruction, int slot)
	{
		return switch(slot)
		{
			case 0 -> // backplane
					1;
			case 1 -> // logic
					instruction.getOperator().getComplexity();
			case 2 -> // solder
					(int)Math.ceil((instruction.getOperator().getComplexity()+instruction.getInputs().length+1)/2f);
			default -> -1;
		};
	}

	public boolean canUseGui(Player player)
	{
		return true;
	}

	public CircuitTableBlockEntity getGuiMaster()
	{
		return master();
	}

	public ArgContainer<CircuitTableBlockEntity, ?> getContainerType()
	{
		return IEMenuTypes.CIRCUIT_TABLE;
	}

	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	public boolean isStackValid(int slot, ItemStack stack)
	{
		return true;
	}

	public int getSlotLimit(int slot)
	{
		return 64;
	}

	public void doGraphicalUpdates()
	{
		this.setChanged();
	}

	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return false;
	}

	public EnumProperty<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	@Nullable
	public CircuitTableBlockEntity master()
	{
		if(!isDummy())
			return this;
		// Used to provide tile-dependant drops after breaking
		if(tempMasterBE!=null)
			return (CircuitTableBlockEntity)tempMasterBE;
		Direction dummyDir = isDummy()?getFacing().getCounterClockWise(): getFacing().getClockWise();
		BlockPos masterPos = getBlockPos().relative(dummyDir);
		BlockEntity te = Utils.getExistingTileEntity(level, masterPos);
		return (te instanceof CircuitTableBlockEntity)?(CircuitTableBlockEntity)te: null;
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

	public BlockPos getModelOffset(BlockState state, @Nullable Vec3i size)
	{
		if(isDummy())
			return DUMMY_POS;
		else
			return MASTER_POS;
	}

	private final MultiblockCapability<IEnergyStorage> energyCap = MultiblockCapability.make(
			this, be -> be.energyCap, CircuitTableBlockEntity::master, makeEnergyInput(energyStorage)
	);

	public static void registerCapabilities(BECapabilityRegistrar<CircuitTableBlockEntity> registrar)
	{
		registrar.register(Energy.BLOCK, (be, side) -> {
			if(side==null||side==be.getFacing()&&be.isDummy())
				return be.energyCap.get();
			else
				return null;
		});
	}
}
