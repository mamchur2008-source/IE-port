package blusunrize.immersiveengineering.common.util;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

public class ContainerHelperCompat
{
	public static void loadAllItems(CompoundTag nbt, NonNullList<ItemStack> inventory, Provider provider)
	{
		ContainerHelper.loadAllItems(TagValueInput.create(ProblemReporter.DISCARDING, provider, nbt), inventory);
	}

	public static void saveAllItems(CompoundTag nbt, NonNullList<ItemStack> inventory, Provider provider)
	{
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
		ContainerHelper.saveAllItems(output, inventory);
		nbt.merge(output.buildResult());
	}
}
