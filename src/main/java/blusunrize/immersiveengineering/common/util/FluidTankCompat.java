package blusunrize.immersiveengineering.common.util;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import blusunrize.immersiveengineering.common.util.inventory.MultiFluidTank;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class FluidTankCompat
{
	public static CompoundTag writeToNBT(ValueIOSerializable tank, Provider provider)
	{
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
		tank.serialize(output);
		return output.buildResult();
	}

	public static void readFromNBT(ValueIOSerializable tank, Provider provider, CompoundTag nbt)
	{
		tank.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, provider, nbt));
	}

	public static CompoundTag writeToNBT(MultiFluidTank tank, Provider provider)
	{
		return tank.writeToNBT(new CompoundTag(), provider);
	}

	public static void readFromNBT(MultiFluidTank tank, Provider provider, CompoundTag nbt)
	{
		tank.readFromNBT(nbt, provider);
	}
}
