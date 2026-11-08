/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.energy;

import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class NullEnergyStorage implements IEnergyStorage, EnergyHandler
{
	public static IEnergyStorage INSTANCE = new NullEnergyStorage();

	private NullEnergyStorage()
	{
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate)
	{
		return 0;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate)
	{
		return 0;
	}

	@Override
	public int getEnergyStored()
	{
		return 0;
	}

	@Override
	public int getMaxEnergyStored()
	{
		return 0;
	}

	@Override
	public boolean canExtract()
	{
		return false;
	}

	@Override
	public boolean canReceive()
	{
		return false;
	}

	@Override
	public long getAmountAsLong()
	{
		return 0;
	}

	@Override
	public long getCapacityAsLong()
	{
		return 0;
	}

	@Override
	public int insert(int amount, TransactionContext transaction)
	{
		TransferPreconditions.checkNonNegative(amount);
		return 0;
	}

	@Override
	public int extract(int amount, TransactionContext transaction)
	{
		TransferPreconditions.checkNonNegative(amount);
		return 0;
	}
}
