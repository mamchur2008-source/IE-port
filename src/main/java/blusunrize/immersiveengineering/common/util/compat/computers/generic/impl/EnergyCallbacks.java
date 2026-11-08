/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.impl;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities.Energy;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyCallbacks extends Callback<BlockEntity>
{
	public static final EnergyCallbacks INSTANCE = new EnergyCallbacks();

	@ComputerCallable
	public int getMaxEnergyStored(CallbackEnvironment<BlockEntity> env)
	{
		return 0;
	}

	@ComputerCallable
	public int getEnergyStored(CallbackEnvironment<BlockEntity> env)
	{
		return 0;
	}
}
