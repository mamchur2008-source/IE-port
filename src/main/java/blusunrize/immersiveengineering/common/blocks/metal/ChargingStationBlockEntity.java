/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.capabilities.Capabilities.Energy;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;

public class ChargingStationBlockEntity extends IEBaseBlockEntity implements IEClientTickableBE, IEServerTickableBE,
		IIEInventory, IStateBasedDirectional, IBlockBounds, IComparatorOverride, IPlayerInteraction
{
	public AveragingEnergyStorage energyStorage = new AveragingEnergyStorage(32000);
	public NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
	private boolean charging = true;
	public int comparatorOutput = 0;
	private final IEnergyStorage energyCap = makeEnergyInput(energyStorage);

	public ChargingStationBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.CHARGING_STATION.get(), pos, state);
	}

	public void tickClient()
	{
		IEnergyStorage itemEnergy = blusunrize.immersiveengineering.common.util.CapabilityCompat.getItemCapability(inventory.get(0), Energy.ITEM);
		if(itemEnergy!=null&&charging)
		{
			float charge = 0;
			float max = itemEnergy.getMaxEnergyStored();
			if(max > 0)
				charge = itemEnergy.getEnergyStored()/max;

			for(int i = 0; i < 3; i++)
			{
				long time = level.getGameTime();
				if(charge >= 1||(time%12 >= i*4&&time%12 <= i*4+2))
				{
					int shift = i-1;
					double x = getBlockPos().getX()+.5+(getFacing()==Direction.WEST?-.46875: getFacing()==Direction.EAST?.46875: getFacing()==Direction.NORTH?(-.1875*shift): (.1875*shift));
					double y = getBlockPos().getY()+.25;
					double z = getBlockPos().getZ()+.5+(getFacing()==Direction.NORTH?-.46875: getFacing()==Direction.SOUTH?.46875: getFacing()==Direction.EAST?(-.1875*shift): (.1875*shift));
					int color = ((int)((1-charge)*255)&255)<<16|((int)(charge*255)&255)<<8;
					level.addParticle(new DustParticleOptions(color, .5f), x, y, z, .25, .25, .25);
				}
			}
		}
	}

	public void tickServer()
	{
		this.energyStorage.updateAverage();
		IEnergyStorage itemEnergy = blusunrize.immersiveengineering.common.util.CapabilityCompat.getItemCapability(inventory.get(0), Energy.ITEM);
		if(itemEnergy!=null)
		{
			if(charging)
			{
				if(energyStorage.getEnergyStored()==0)
				{
					charging = false;
					this.markContainingBlockForUpdate(null);
					return;
				}
				int stored = itemEnergy.getEnergyStored();
				int max = itemEnergy.getMaxEnergyStored();
				int space = max-stored;
				if(space > 0)
				{
					int energyDec = (10*stored)/max;
					int insert = Math.min(space, Math.max(energyStorage.getAverageInsertion(), IEServerConfig.MACHINES.charger_consumption.get()));
					int accepted = Math.min(itemEnergy.receiveEnergy(insert, true), this.energyStorage.extractEnergy(insert, true));
					if((accepted = this.energyStorage.extractEnergy(accepted, false)) > 0)
						stored += itemEnergy.receiveEnergy(accepted, false);
					int energyDecNew = (10*stored)/max;
					if(energyDec!=energyDecNew)
						this.markContainingBlockForUpdate(null);
				}
			}
			else if(energyStorage.getEnergyStored() >= energyStorage.getMaxEnergyStored()*.95)
			{
				charging = true;
				this.markContainingBlockForUpdate(null);
			}
		}


		if(level.getGameTime()%32==((getBlockPos().getX()^getBlockPos().getZ())&31))
		{
			float charge = 0;
			if(itemEnergy!=null)
			{
				float max = itemEnergy.getMaxEnergyStored();
				if(max > 0)
					charge = itemEnergy.getEnergyStored()/max;
			}
			int i = (int)(15*charge);
			if(i!=this.comparatorOutput)
			{
				this.comparatorOutput = i;
				level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
			}
		}
	}

	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		EnergyHelper.deserializeFrom(energyStorage, nbt, provider);
		inventory.set(0, blusunrize.immersiveengineering.common.util.ItemStackCompat.parseOptional(provider, nbt.getCompoundOrEmpty("inventory")));
		charging = nbt.getBooleanOr("charging", false);
	}

	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		EnergyHelper.serializeTo(energyStorage, nbt, provider);
		nbt.putBoolean("charging", charging);
		if(!inventory.get(0).isEmpty())
			nbt.put("inventory", blusunrize.immersiveengineering.common.util.ItemStackCompat.saveOptional(inventory.get(0), provider));
	}

	public boolean triggerEvent(int id, int arg)
	{
		if(id==0)
		{
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	public int getComparatorInputOverride()
	{
		return this.comparatorOutput;
	}

	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return true;
	}

	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return Shapes.box(
				getFacing().getAxis()==Axis.X?0: .125f, 0, getFacing().getAxis()==Axis.Z?0: .125f,
				getFacing().getAxis()==Axis.X?1: .875f, 1, getFacing().getAxis()==Axis.Z?1: .875f
		);
	}

	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	public boolean isStackValid(int slot, ItemStack stack)
	{
		return EnergyHelper.isFluxReceiver(stack);
	}

	public int getSlotLimit(int slot)
	{
		return 1;
	}

	public void doGraphicalUpdates()
	{
		this.setChanged();
		this.markContainingBlockForUpdate(null);
	}

	private final IItemHandler insertionHandler = new IEInventoryHandler(1, this);

	public static void registerCapabilities(BECapabilityRegistrar<ChargingStationBlockEntity> registrar)
	{
		registrar.register(Energy.BLOCK, (be, facing) -> {
			if(facing==null||facing==Direction.DOWN||facing==be.getFacing().getOpposite())
				return be.energyCap;
			else
				return null;
		});
		registrar.register(Capabilities.Item.BLOCK, (be, facing) -> be.insertionHandler);
	}

	public InteractionResult interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(isStackValid(0, heldItem))
		{
			ItemStack stored = !inventory.get(0).isEmpty()?inventory.get(0).copy(): ItemStack.EMPTY;
			inventory.set(0, heldItem.copy());
			player.setItemInHand(hand, stored);
			setChanged();
			this.markContainingBlockForUpdate(null);
			return InteractionResult.SUCCESS;
		}
		else if(!inventory.get(0).isEmpty())
		{
			if(level instanceof net.minecraft.server.level.ServerLevel serverLevel)
				player.spawnAtLocation(serverLevel, inventory.get(0).copy());
			inventory.set(0, ItemStack.EMPTY);
			setChanged();
			this.markContainingBlockForUpdate(null);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
}
