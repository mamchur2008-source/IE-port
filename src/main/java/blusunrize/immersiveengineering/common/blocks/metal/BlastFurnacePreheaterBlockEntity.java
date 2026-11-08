/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundBE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.capabilities.Capabilities.Energy;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class BlastFurnacePreheaterBlockEntity extends IEBaseBlockEntity implements IStateBasedDirectional,
		IHasDummyBlocks, IModelOffsetProvider, IEClientTickableBE, ISoundBE
{
	public static final float ANGLE_PER_TICK = (float)Math.toRadians(20);
	public boolean active;
	public int dummy = 0;
	public final MutableEnergyStorage energyStorage = new MutableEnergyStorage(8000);
	public float angle = 0;
	private final MultiblockCapability<IEnergyStorage> energyCap = MultiblockCapability.make(
			this, be -> be.energyCap, BlastFurnacePreheaterBlockEntity::master, makeEnergyInput(energyStorage)
	);

	public BlastFurnacePreheaterBlockEntity(BlockEntityType<BlastFurnacePreheaterBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	public int doSpeedup()
	{
		int consumed = IEServerConfig.MACHINES.preheater_consumption.get();
		if(this.energyStorage.extractEnergy(consumed, true)==consumed)
		{
			if(!active)
			{
				active = true;
				this.markContainingBlockForUpdate(null);
			}
			this.energyStorage.extractEnergy(consumed, false);
			return 1;
		}
		else
			turnOff();
		return 0;
	}

	public void tickClient()
	{
		if(active)
			angle = (angle+ANGLE_PER_TICK)%Mth.PI;
		ImmersiveEngineering.proxy.handleTileSound(IESounds.preheater, this, active, 0.5f, 1f);
	}

	public Void turnOff()
	{
		if(active)
		{
			active = false;
			this.markContainingBlockForUpdate(null);
		}
		return null;
	}

	public boolean isDummy()
	{
		return dummy > 0;
	}

	@Override
	public void onLoad()
	{
		super.onLoad();
		if(dummy==0&&getBlockState().getValue(IEProperties.MULTIBLOCKSLAVE))
			recoverDummyOffset();
	}

	private void recoverDummyOffset()
	{
		for(int i = 1; i <= 2; i++)
		{
			BlockPos masterPos = worldPosition.below(i);
			BlockState masterState = level.getBlockState(masterPos);
			if(masterState.is(getBlockState().getBlock())&&!masterState.getValue(IEProperties.MULTIBLOCKSLAVE)
					&&level.getBlockEntity(masterPos) instanceof BlastFurnacePreheaterBlockEntity master)
			{
				dummy = i;
				setFacing(master.getFacing());
				setChanged();
				markContainingBlockForUpdate(null);
				return;
			}
		}
	}

	@Nullable
	public BlastFurnacePreheaterBlockEntity master()
	{
		if(!isDummy())
			return this;
		BlockPos masterPos = getBlockPos().below(dummy);
		BlockEntity te = Utils.getExistingTileEntity(level, masterPos);
		return te instanceof BlastFurnacePreheaterBlockEntity heater?heater: null;
	}

	public void placeDummies(BlockPlaceContext ctx, BlockState state)
	{
		state = state.setValue(IEProperties.MULTIBLOCKSLAVE, true);
		for(int i = 1; i <= 2; i++)
		{
			BlockPos dummyPos = worldPosition.above(i);
			level.setBlockAndUpdate(dummyPos, state);
			if(level.getBlockEntity(dummyPos) instanceof BlastFurnacePreheaterBlockEntity dummyBlock)
			{
				dummyBlock.dummy = i;
				dummyBlock.setFacing(this.getFacing());
				dummyBlock.setChanged();
			}
		}
	}

	public void breakDummies(BlockPos pos, BlockState state)
	{
		int dummyOffset = getDummyOffsetForBreaking(pos, state);
		BlockPos masterPos = pos.below(dummyOffset);
		for(int i = 0; i <= 2; i++)
		{
			BlockPos blockPos = masterPos.above(i);
			if(level.getBlockEntity(blockPos) instanceof BlastFurnacePreheaterBlockEntity)
				level.removeBlock(blockPos, false);
		}
	}

	private int getDummyOffsetForBreaking(BlockPos pos, BlockState state)
	{
		if(dummy > 0)
			return dummy;
		if(state.hasProperty(IEProperties.MULTIBLOCKSLAVE)&&state.getValue(IEProperties.MULTIBLOCKSLAVE))
			for(int i = 1; i <= 2; i++)
			{
				BlockState masterState = level.getBlockState(pos.below(i));
				if(masterState.is(state.getBlock())&&masterState.hasProperty(IEProperties.MULTIBLOCKSLAVE)
						&&!masterState.getValue(IEProperties.MULTIBLOCKSLAVE))
					return i;
			}
		return 0;
	}

	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		dummy = nbt.getIntOr("dummy", 0);
		active = nbt.getBooleanOr("active", false);
		if(descPacket)
			this.markContainingBlockForUpdate(null);
		else
			EnergyHelper.deserializeFrom(energyStorage, nbt, provider);
	}

	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		nbt.putInt("dummy", dummy);
		nbt.putBoolean("active", active);
		if(!descPacket)
			EnergyHelper.serializeTo(energyStorage, nbt, provider);
	}

	public static void registerCapabilities(BECapabilityRegistrar<BlastFurnacePreheaterBlockEntity> registrar)
	{
		registrar.register(Energy.BLOCK, (be, side) -> {
			if(side==null&&!be.isDummy()||(be.dummy==2&&side==Direction.UP))
				return be.energyCap.get();
			else
				return null;
		});
	}

	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	public void afterRotation(Direction oldDir, Direction newDir)
	{
		for(int i = 0; i <= 2; i++)
		{
			BlockEntity te = level.getBlockEntity(getBlockPos().offset(0, -dummy+i, 0));
			if(te instanceof BlastFurnacePreheaterBlockEntity dummy)
			{
				dummy.setFacing(newDir);
				dummy.setChanged();
				dummy.markContainingBlockForUpdate(null);
			}
		}
	}

	public BlockPos getModelOffset(BlockState state, @Nullable Vec3i size)
	{
		return new BlockPos(0, dummy, 0);
	}

	public boolean shouldPlaySound(String sound)
	{
		return active;
	}
}
