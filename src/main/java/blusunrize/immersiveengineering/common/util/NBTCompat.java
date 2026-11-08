package blusunrize.immersiveengineering.common.util;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;

public class NBTCompat
{
	public static TagValueOutput createOutput(Provider provider)
	{
		return TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
	}

	public static ValueInput createInput(Provider provider, CompoundTag tag)
	{
		return TagValueInput.create(ProblemReporter.DISCARDING, provider, tag);
	}

	public static CompoundTag getCompound(CompoundTag tag, String key)
	{
		return tag.getCompoundOrEmpty(key);
	}

	public static ListTag getList(CompoundTag tag, String key)
	{
		return tag.getListOrEmpty(key);
	}
}
