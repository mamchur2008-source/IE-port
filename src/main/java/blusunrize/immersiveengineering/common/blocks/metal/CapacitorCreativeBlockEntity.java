/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Machines.CapacitorConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class CapacitorCreativeBlockEntity extends CapacitorBlockEntity
{
	public CapacitorCreativeBlockEntity(BlockPos pos, BlockState state)
	{
		super(CapacitorConfig.CREATIVE, pos, state);
		for(Direction d : DirectionUtils.VALUES)
			sideConfig.put(d, IOSideConfig.OUTPUT);
	}

	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.readCustomNBT(nbt, descPacket, provider);
		for(Direction d : DirectionUtils.VALUES)
			if(!nbt.contains("sideConfig_"+d.ordinal()))
				sideConfig.put(d, IOSideConfig.OUTPUT);
	}

	protected IEnergyStorage makeMainEnergyStorage()
	{
		return InfiniteEnergyStorage.INSTANCE;
	}

	private static class InfiniteEnergyStorage implements IEnergyStorage
	{
		public static final IEnergyStorage INSTANCE = new InfiniteEnergyStorage();

		public int receiveEnergy(int maxReceive, boolean simulate)
		{
			return maxReceive;
		}

		public int extractEnergy(int maxExtract, boolean simulate)
		{
			return maxExtract;
		}

		public int getEnergyStored()
		{
			return Integer.MAX_VALUE;
		}

		public int getMaxEnergyStored()
		{
			return Integer.MAX_VALUE;
		}

		public boolean canExtract()
		{
			return true;
		}

		public boolean canReceive()
		{
			return true;
		}
	}
}
